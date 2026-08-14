package com.example.nativeminds.audio

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.SpokenRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val PARAGRAPHS = listOf("One. Two. Three.", "Four. Five.")
private val SENTENCES = listOf("One.", "Two.", "Three.", "Four.", "Five.")

/** Drives the queue the way the engine does: it announces each utterance as it begins speaking. */
private fun NarrationQueue.speakThrough(utterances: List<NarrationQueue.Utterance>, upTo: Int) {
    utterances.take(upTo + 1).forEach { onUtteranceStarted(it.id) }
}

class NarrationQueueTest {
    @Test
    fun `starting hands the whole lesson to the engine in order`() {
        val queue = NarrationQueue()

        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)

        assertEquals(SENTENCES, utterances.map { it.text })
        assertEquals(NarrationState.Playing(7, sentenceIndex = 0, totalSentences = 5), queue.state)
    }

    @Test
    fun `the position follows what the engine reports it is speaking`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)

        queue.speakThrough(utterances, upTo = 2)

        assertEquals(NarrationState.Playing(7, sentenceIndex = 2, totalSentences = 5), queue.state)
    }

    @Test
    fun `resuming continues from the paused sentence rather than the beginning`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.speakThrough(utterances, upTo = 2)

        assertTrue(queue.pause())
        assertEquals(NarrationState.Paused(7, sentenceIndex = 2, totalSentences = 5), queue.state)

        val remaining = queue.resume()

        assertEquals(listOf("Three.", "Four.", "Five."), remaining.map { it.text })
        assertEquals(NarrationState.Playing(7, sentenceIndex = 2, totalSentences = 5), queue.state)
    }

    @Test
    fun `resuming picks up mid-sentence from where speech actually reached`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = listOf("Hello there friend."))
        queue.onUtteranceStarted(utterances[0].id)

        queue.onRangeStart(utterances[0].id, start = 6, end = 11)
        queue.pause()

        assertEquals(listOf("there friend."), queue.resume().map { it.text })
    }

    @Test
    fun `a resumed sentence keeps reporting its position against the whole sentence`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = listOf("Hello there friend."))
        queue.onUtteranceStarted(utterances[0].id)
        queue.onRangeStart(utterances[0].id, start = 6, end = 11)
        queue.pause()

        val resumed = queue.resume()
        queue.onUtteranceStarted(resumed[0].id)
        queue.onRangeStart(resumed[0].id, start = 6, end = 12)
        queue.pause()

        assertEquals(listOf("friend."), queue.resume().map { it.text })
    }

    @Test
    fun `the reported range is published so a reader can follow the words`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = listOf("Hello there friend."))
        queue.onUtteranceStarted(utterances[0].id)

        queue.onRangeStart(utterances[0].id, start = 6, end = 11)

        assertEquals(SpokenRange(6, 11), (queue.state as NarrationState.Playing).spokenRange)
    }

    @Test
    fun `a resumed utterance reports its range against the whole sentence`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = listOf("Hello there friend."))
        queue.onUtteranceStarted(utterances[0].id)
        queue.onRangeStart(utterances[0].id, start = 6, end = 11)
        queue.pause()

        val resumed = queue.resume()
        queue.onUtteranceStarted(resumed[0].id)
        queue.onRangeStart(resumed[0].id, start = 6, end = 12)

        assertEquals(SpokenRange(12, 18), (queue.state as NarrationState.Playing).spokenRange)
    }

    @Test
    fun `a device that cannot report words marks the whole sentence instead`() {
        val queue = NarrationQueue(reportsWordRanges = false)

        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)

        assertEquals(SpokenRange(0, 4), (queue.state as NarrationState.Playing).spokenRange)

        queue.onUtteranceStarted(utterances[2].id)

        assertEquals(SpokenRange(0, 6), (queue.state as NarrationState.Playing).spokenRange)
    }

    @Test
    fun `a new sentence clears the range until the engine reports one`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.onUtteranceStarted(utterances[0].id)
        queue.onRangeStart(utterances[0].id, start = 0, end = 3)

        queue.onUtteranceStarted(utterances[1].id)

        assertEquals(null, (queue.state as NarrationState.Playing).spokenRange)
    }

    @Test
    fun `pausing keeps the range so the highlight stays where speech stopped`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = listOf("Hello there friend."))
        queue.onUtteranceStarted(utterances[0].id)
        queue.onRangeStart(utterances[0].id, start = 6, end = 11)

        queue.pause()

        assertEquals(SpokenRange(6, 11), (queue.state as NarrationState.Paused).spokenRange)
    }

    @Test
    fun `callbacks from the speech a pause abandoned are ignored`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.speakThrough(utterances, upTo = 1)
        queue.pause()

        queue.onUtteranceStarted(utterances[4].id)
        queue.onRangeStart(utterances[4].id, start = 3, end = 5)
        queue.onUtteranceFinished(utterances[4].id)

        assertEquals(NarrationState.Paused(7, sentenceIndex = 1, totalSentences = 5), queue.state)
        assertEquals(listOf("Two.", "Three.", "Four.", "Five."), queue.resume().map { it.text })
    }

    @Test
    fun `pausing twice is a no-op that keeps the position`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.speakThrough(utterances, upTo = 1)
        queue.pause()

        assertFalse(queue.pause())
        assertEquals(NarrationState.Paused(7, sentenceIndex = 1, totalSentences = 5), queue.state)
    }

    @Test
    fun `resuming when nothing is paused does nothing`() {
        val queue = NarrationQueue()

        assertEquals(emptyList<NarrationQueue.Utterance>(), queue.resume())
        assertEquals(NarrationState.Idle, queue.state)
    }

    @Test
    fun `finishing the final sentence returns to idle`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.speakThrough(utterances, upTo = 4)

        queue.onUtteranceFinished(utterances[3].id)
        assertTrue(queue.state is NarrationState.Playing)

        queue.onUtteranceFinished(utterances[4].id)
        assertEquals(NarrationState.Idle, queue.state)
    }

    @Test
    fun `stopping discards the position so the next start begins again`() {
        val queue = NarrationQueue()
        val utterances = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.speakThrough(utterances, upTo = 2)
        queue.pause()

        queue.stop()
        assertEquals(NarrationState.Idle, queue.state)

        assertEquals(SENTENCES, queue.start(lessonId = 7, paragraphs = PARAGRAPHS).map { it.text })
    }

    @Test
    fun `starting another lesson abandons the previous position`() {
        val queue = NarrationQueue()
        val first = queue.start(lessonId = 7, paragraphs = PARAGRAPHS)
        queue.speakThrough(first, upTo = 2)

        queue.start(lessonId = 9, paragraphs = listOf("Other lesson."))
        queue.onUtteranceStarted(first[4].id)

        assertEquals(NarrationState.Playing(9, sentenceIndex = 0, totalSentences = 1), queue.state)
    }

    @Test
    fun `an empty lesson goes straight back to idle`() {
        val queue = NarrationQueue()

        assertEquals(emptyList<NarrationQueue.Utterance>(), queue.start(7, listOf("   ")))
        assertEquals(NarrationState.Idle, queue.state)
    }
}
