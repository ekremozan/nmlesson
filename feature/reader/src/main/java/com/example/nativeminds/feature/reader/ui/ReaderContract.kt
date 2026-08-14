package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.SpokenRange
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.domain.narration.wordCount
import com.example.nativeminds.feature.reader.ui.model.ListenPillStatus
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderLessonUiModel

/**
 * Everything the reader screen can be showing.
 *
 * Split out of [ReaderUiState] so the three cases are mutually exclusive by construction — there
 * is no combination of flags that produces "loading and unavailable at once".
 */
sealed interface ReaderContentUiState {
    data object Loading : ReaderContentUiState

    data class Ready(
        val lesson: ReaderLessonUiModel,
        val body: ReaderBodyUiModel,
    ) : ReaderContentUiState

    data class Unavailable(val reason: UnavailableReason) : ReaderContentUiState
}

/**
 * The reader's state — irreducible facts only. Anything that can be worked out from these is a
 * computed property rather than a field the reducer has to keep in step.
 */
data class ReaderUiState(
    val lessonId: Long,
    val content: ReaderContentUiState = ReaderContentUiState.Loading,
    val progressPercent: Int = 0,
    /**
     * Incremented by a retry so the load key changes and the content flow re-subscribes.
     *
     * A count of how many times the reader has asked, which is a fact about the world rather than
     * derived data — it belongs in state precisely so retry stays a pure transition.
     */
    val retryToken: Int = 0,
    /**
     * This screen visit's narration, scoped to [lessonId] by
     * [com.example.nativeminds.domain.usecase.ObserveNarrationUseCase] before it ever reaches the
     * reducer — always [NarrationState.Idle] for a freshly created screen, which is what makes
     * leaving and returning to the reader reset playback without any reducer code of its own.
     */
    val narration: NarrationState = NarrationState.Idle,
) {
    val readyContent: ReaderContentUiState.Ready?
        get() = content as? ReaderContentUiState.Ready

    val isRestricted: Boolean
        get() = readyContent?.body?.isTruncated == true

    val listenPillStatus: ListenPillStatus
        get() = when (narration) {
            NarrationState.Idle, is NarrationState.Unavailable -> ListenPillStatus.IDLE
            is NarrationState.Playing -> ListenPillStatus.PLAYING
            is NarrationState.Paused -> ListenPillStatus.PAUSED
        }

    /**
     * Where narration currently is, expressed as characters of a rendered paragraph.
     *
     * Derived rather than stored: the narration position and the body's sentence segmentation are
     * both already here, so a field would only be a second copy for the reducer to keep in step.
     *
     * No [SpokenRange] means nothing is marked. The narrator publishes the whole sentence itself
     * where the platform cannot report words; guessing that here instead would also cover the
     * engine's start-up latency on every device, which flashed the sentence over the page each
     * time narration started, resumed or crossed a sentence boundary.
     */
    val narrationHighlight: NarrationHighlight?
        get() {
            val sentenceIndex = narration.sentenceIndexOrNull() ?: return null
            val spoken = narration.spokenRangeOrNull() ?: return null
            val body = readyContent?.body ?: return null
            val sentence = body.sentences.getOrNull(sentenceIndex) ?: return null
            val start = (sentence.start + spoken.start).coerceIn(sentence.start, sentence.end)
            val end = (sentence.start + spoken.end).coerceIn(start, sentence.end)
            if (start == end) return null
            return NarrationHighlight(sentence.paragraphIndex, start, end)
        }

    /**
     * How far narration has spoken through the lesson, as a fraction of its words.
     *
     * Words rather than sentences because sentences vary wildly in length — a bar stepping by one
     * twelfth per sentence lurches, while a word is small enough that the bar moves with the voice.
     * Zero until narration starts: this is a measure of listening alone, and [listenPillProgress]
     * is the one that decides when listening is what the reader is doing.
     */
    val narrationProgress: Float
        get() {
            val sentenceIndex = narration.sentenceIndexOrNull() ?: return 0f
            val body = readyContent?.body ?: return 0f
            val totalWords = body.wordTotals.lastOrNull()?.takeIf { it > 0 } ?: return 0f
            val wordsBefore = body.wordTotals.getOrNull(sentenceIndex) ?: return 0f
            val sentence = body.sentences.getOrNull(sentenceIndex) ?: return 0f
            val spoken = narration.spokenRangeOrNull()

            val spokenHere = if (spoken == null) {
                0
            } else {
                val until = (sentence.start + spoken.end).coerceIn(sentence.start, sentence.end)
                body.paragraphs[sentence.paragraphIndex]
                    .substring(sentence.start, until)
                    .wordCount()
            }
            return ((wordsBefore + spokenHere).toFloat() / totalWords).coerceIn(0f, 1f)
        }

    /**
     * What the pill's bar shows: the voice while there is one, the scroll otherwise.
     *
     * The bar answers "how far through this lesson am I", and which way the reader is getting
     * through it is exactly what narration being live tells us. A paused lesson still counts as
     * listening — the reader is coming back to where the voice stopped, not to where the page
     * happens to be scrolled.
     */
    val listenPillProgress: Float
        get() = when (narration) {
            is NarrationState.Playing, is NarrationState.Paused -> narrationProgress
            else -> progressPercent.toFloat() / PERCENT_COMPLETE
        }
}

private const val PERCENT_COMPLETE = 100

/**
 * The characters of one paragraph to draw the narration highlight over, [end] exclusive.
 *
 * A single range rather than a per-paragraph map because narration only ever speaks one place at
 * a time — every other paragraph is drawn plain.
 */
data class NarrationHighlight(
    val paragraphIndex: Int,
    val start: Int,
    val end: Int,
)

private fun NarrationState.sentenceIndexOrNull(): Int? = when (this) {
    is NarrationState.Playing -> sentenceIndex
    is NarrationState.Paused -> sentenceIndex
    else -> null
}

private fun NarrationState.spokenRangeOrNull(): SpokenRange? = when (this) {
    is NarrationState.Playing -> spokenRange
    is NarrationState.Paused -> spokenRange
    else -> null
}

sealed interface ReaderIntent {
    /** Content arriving from the domain layer, folded in through the same door as user actions. */
    data class DetailChanged(val detail: ReaderDetail) : ReaderIntent

    data object RetryRequested : ReaderIntent

    data class ScrollProgressChanged(val percent: Int) : ReaderIntent

    data object ListenClicked : ReaderIntent

    /** Narration arriving from [com.example.nativeminds.domain.usecase.ObserveNarrationUseCase]. */
    data class NarrationStateChanged(val narration: NarrationState) : ReaderIntent
}

/**
 * The one effect meant for the screen itself — everything else in [ReaderEffect] is executed by
 * the ViewModel directly and never reaches the screen's effect collector.
 */
sealed interface ReaderUiEffect {
    data object ShowAudioUnavailable : ReaderUiEffect
}

sealed interface ReaderEffect {
    data object ShowAudioUnavailable : ReaderEffect

    /**
     * The three narration actions below are not one-shot UI events like [ShowAudioUnavailable] —
     * the ViewModel executes them directly against
     * [com.example.nativeminds.domain.narration.LessonNarrator] instead of forwarding them to the
     * screen. They still come back from the reducer rather than being decided in the ViewModel,
     * because *which* action [ReaderIntent.ListenClicked] means depends on the current
     * [ReaderUiState.narration] — a decision, and the reducer is the only place allowed to make
     * one.
     */
    data class StartNarration(val paragraphs: List<String>) : ReaderEffect

    data object PauseNarration : ReaderEffect

    data object ResumeNarration : ReaderEffect
}

/**
 * What a reduction produces: the next state, and any one-shot effects that intent raised.
 *
 * Effects come back from the reducer rather than being sent by the ViewModel because the mapping
 * from intent to effect is a decision, and the ViewModel is not allowed to make decisions — it
 * would have to branch on the intent to do so. Here the decision stays in the one pure, testable
 * place that already owns every other consequence of an intent.
 */
data class Reduction(
    val state: ReaderUiState,
    val effects: List<ReaderEffect> = emptyList(),
)
