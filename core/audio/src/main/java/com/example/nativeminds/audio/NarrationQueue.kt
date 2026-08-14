package com.example.nativeminds.audio

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.NarrationUnavailableReason
import com.example.nativeminds.domain.model.SpokenRange
import com.example.nativeminds.domain.narration.sentenceTexts

/**
 * Where narration has got to in a story, and what the speech engine should be given next.
 *
 * Pure and Android-free on purpose, the same reasoning the feature reducers follow: "pause here,
 * resume from exactly here" is the rule this whole feature is judged on, and here it is provable
 * in a plain JVM test instead of only on a device with a working speech engine.
 *
 * The whole story is handed to the engine at once ([Utterance] per sentence) rather than one
 * sentence at a time. Feeding the next sentence only after the previous one reports completion
 * made playback depend on a callback some engines never deliver — narration would fall silent
 * after the first sentence with the position frozen at zero, so pausing and resuming replayed the
 * opening line and looked exactly like starting over. Progress is therefore tracked from
 * [onUtteranceStarted] and [onRangeStart], which fire as speech happens, and completion is only
 * consulted to notice the story has ended.
 *
 * Not thread-safe by design — [TextToSpeechNarrator] confines every call to the main thread, which
 * is what stops a speech-engine callback (delivered on a binder thread) from racing a tap.
 */
internal class NarrationQueue(private val reportsWordRanges: Boolean = true) {
    /** A sentence, or the tail of one, to hand to the engine. */
    data class Utterance(val text: String, val id: String)

    var state: NarrationState = NarrationState.Idle
        private set

    private var sentences: List<String> = emptyList()
    private var storyId: Long = 0
    private var sentenceIndex: Int = 0

    /** How far into [sentenceIndex] speech has actually reached, in characters. */
    private var offsetInSentence: Int = 0

    /**
     * The words being said right now, for a caller that follows along with a highlight.
     *
     * Null means "not known yet" and nothing should be marked — never "mark the whole sentence".
     * The gap between handing a sentence to the engine and the engine reporting its first word is
     * the engine's own start-up latency, and painting the whole sentence across it flashed a block
     * of colour over the page on every start, resume and sentence boundary. Where the platform
     * cannot report words at all ([reportsWordRanges] false, below API 26) the sentence range is
     * published deliberately instead, which is a highlight that is coarse rather than one that is
     * briefly wrong.
     */
    private var spokenRange: SpokenRange? = null

    /**
     * Bumped by every transition and embedded in each [Utterance.id].
     *
     * A callback carrying a stale token belongs to speech that pausing, stopping, or starting
     * another story has already superseded — including the completion the platform reports for the
     * utterance a pause interrupted, which would otherwise move the reader past the words they
     * expect to come back to.
     */
    private var token: Int = 0

    fun start(storyId: Long, paragraphs: List<String>): List<Utterance> {
        token++
        this.storyId = storyId
        sentences = paragraphs.sentenceTexts()
        sentenceIndex = 0
        offsetInSentence = 0
        if (sentences.isEmpty()) {
            spokenRange = null
            state = NarrationState.Idle
            return emptyList()
        }
        spokenRange = wholeSentenceRangeOrNull()
        state = NarrationState.Playing(storyId, 0, sentences.size, spokenRange)
        return utterancesFrom(fromIndex = 0, offset = 0)
    }

    /** True when there was playback to interrupt, so the caller should stop the engine. */
    fun pause(): Boolean {
        if (state !is NarrationState.Playing) return false
        token++
        state = NarrationState.Paused(storyId, sentenceIndex, sentences.size, spokenRange)
        return true
    }

    fun resume(): List<Utterance> {
        if (state !is NarrationState.Paused) return emptyList()
        token++
        state = NarrationState.Playing(storyId, sentenceIndex, sentences.size, spokenRange)
        return utterancesFrom(sentenceIndex, offsetInSentence)
    }

    fun stop() {
        token++
        sentences = emptyList()
        sentenceIndex = 0
        offsetInSentence = 0
        spokenRange = null
        state = NarrationState.Idle
    }

    fun markUnavailable(storyId: Long, reason: NarrationUnavailableReason) {
        token++
        sentences = emptyList()
        spokenRange = null
        state = NarrationState.Unavailable(storyId, reason)
    }

    /** The engine has begun a sentence — the position that survives a pause. */
    fun onUtteranceStarted(utteranceId: String?) {
        val started = decodeUtteranceId(utteranceId) ?: return
        if (started.token != token || state !is NarrationState.Playing) return
        sentenceIndex = started.sentenceIndex
        offsetInSentence = started.offset
        spokenRange = wholeSentenceRangeOrNull()
        state = NarrationState.Playing(storyId, sentenceIndex, sentences.size, spokenRange)
    }

    /**
     * The engine has reached [start]..[end] characters into the utterance it is speaking, which is
     * what lets a resume pick up mid-sentence instead of repeating the whole sentence, and what a
     * reader following along highlights. Only delivered from API 26; below that narration resumes
     * at the last sentence boundary and the highlight stays on the whole sentence.
     */
    fun onRangeStart(utteranceId: String?, start: Int, end: Int) {
        val speaking = decodeUtteranceId(utteranceId) ?: return
        if (speaking.token != token || state !is NarrationState.Playing) return
        offsetInSentence = speaking.offset + start
        spokenRange = SpokenRange(offsetInSentence, speaking.offset + end)
        state = NarrationState.Playing(storyId, sentenceIndex, sentences.size, spokenRange)
    }

    /** Narration is over once the engine finishes the final sentence (FR-009). */
    fun onUtteranceFinished(utteranceId: String?) {
        val finished = decodeUtteranceId(utteranceId) ?: return
        if (finished.token != token || finished.sentenceIndex < sentences.lastIndex) return
        token++
        sentences = emptyList()
        sentenceIndex = 0
        offsetInSentence = 0
        spokenRange = null
        state = NarrationState.Idle
    }

    private fun wholeSentenceRangeOrNull(): SpokenRange? =
        if (reportsWordRanges) {
            null
        } else {
            sentences.getOrNull(sentenceIndex)?.let { SpokenRange(0, it.length) }
        }

    private fun utterancesFrom(fromIndex: Int, offset: Int): List<Utterance> =
        sentences.drop(fromIndex).mapIndexed { position, sentence ->
            val index = fromIndex + position
            val from = if (position == 0) offset.coerceIn(0, sentence.length) else 0
            Utterance(
                text = sentence.substring(from),
                id = encodeUtteranceId(token, storyId, index, from),
            )
        }
}

private data class UtteranceId(val token: Int, val sentenceIndex: Int, val offset: Int)

private fun encodeUtteranceId(token: Int, storyId: Long, sentenceIndex: Int, offset: Int): String =
    "$token:$storyId:$sentenceIndex:$offset"

private fun decodeUtteranceId(raw: String?): UtteranceId? {
    val parts = raw?.split(":") ?: return null
    if (parts.size != 4) return null
    val token = parts[0].toIntOrNull() ?: return null
    val sentenceIndex = parts[2].toIntOrNull() ?: return null
    val offset = parts[3].toIntOrNull() ?: return null
    return UtteranceId(token, sentenceIndex, offset)
}
