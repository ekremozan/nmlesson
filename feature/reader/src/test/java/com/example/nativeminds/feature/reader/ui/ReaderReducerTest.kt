package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.NarrationUnavailableReason
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.SpokenRange
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.feature.reader.ui.model.ListenPillStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderReducerTest {
    @Test
    fun contentArrivingFillsTheScreen() {
        val reduction = initialState().reduce(
            ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess)),
        )

        val content = reduction.state.content as ReaderContentUiState.Ready
        assertEquals(unlockedStory.title, content.story.title)
        assertEquals(storyContent.paragraphs, content.body.paragraphs)
        assertFalse(content.body.isTruncated)
        assertTrue(reduction.effects.isEmpty())
    }

    @Test
    fun anUnavailableStoryReplacesTheBody() {
        val reduction = initialState().reduce(
            ReaderIntent.DetailChanged(
                ReaderDetail.Unavailable(UnavailableReason.STORY_MISSING),
            ),
        )

        assertEquals(
            ReaderContentUiState.Unavailable(UnavailableReason.STORY_MISSING),
            reduction.state.content,
        )
    }

    @Test
    fun restrictedContentMarksTheStateAsRestricted() {
        val reduction = initialState().reduce(
            ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess)),
        )

        assertTrue(reduction.state.isRestricted)
    }

    @Test
    fun gainingEntitlementClearsTheRestriction() {
        val restricted = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess))).state

        val unlocked = restricted
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess))).state

        assertFalse(unlocked.isRestricted)
    }

    @Test
    fun retryReloadsAndBumpsTheLoadToken() {
        val failed = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Unavailable(UnavailableReason.OFFLINE)))
            .state

        val retried = failed.reduce(ReaderIntent.RetryRequested).state

        assertEquals(ReaderContentUiState.Loading, retried.content)
        assertEquals(failed.retryToken + 1, retried.retryToken)
    }

    @Test
    fun progressIsClampedToTheTrack() {
        val over = initialState().reduce(ReaderIntent.ScrollProgressChanged(140)).state
        val under = initialState().reduce(ReaderIntent.ScrollProgressChanged(-20)).state

        assertEquals(100, over.progressPercent)
        assertEquals(0, under.progressPercent)
    }

    @Test
    fun listeningBeforeContentIsReadyChangesNothing() {
        val state = initialState()

        val reduction = state.reduce(ReaderIntent.ListenClicked)

        assertEquals(state, reduction.state)
        assertTrue(reduction.effects.isEmpty())
    }

    @Test
    fun listeningFromIdleStartsNarrationWithTheVisibleParagraphs() {
        val ready = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess))).state

        val reduction = ready.reduce(ReaderIntent.ListenClicked)

        assertEquals(
            listOf(ReaderEffect.StartNarration(storyContent.paragraphs)),
            reduction.effects,
        )
    }

    @Test
    fun listeningWhilePlayingPauses() {
        val playing = readyStateNarrating(
            NarrationState.Playing(unlockedStory.id, sentenceIndex = 2, totalSentences = 5),
        )

        val reduction = playing.reduce(ReaderIntent.ListenClicked)

        assertEquals(listOf(ReaderEffect.PauseNarration), reduction.effects)
    }

    @Test
    fun listeningWhilePausedResumes() {
        val paused = readyStateNarrating(
            NarrationState.Paused(unlockedStory.id, sentenceIndex = 2, totalSentences = 5),
        )

        val reduction = paused.reduce(ReaderIntent.ListenClicked)

        assertEquals(listOf(ReaderEffect.ResumeNarration), reduction.effects)
    }

    @Test
    fun listeningWhenNarrationIsUnavailableShowsTheMessage() {
        val unavailable = readyStateNarrating(
            NarrationState.Unavailable(unlockedStory.id, NarrationUnavailableReason.ENGINE_MISSING),
        )

        val reduction = unavailable.reduce(ReaderIntent.ListenClicked)

        assertEquals(listOf(ReaderEffect.ShowAudioUnavailable), reduction.effects)
    }

    @Test
    fun narrationStateChangedUpdatesThePillStatus() {
        val ready = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess))).state
        assertEquals(ListenPillStatus.IDLE, ready.listenPillStatus)

        val playing = ready.reduce(
            ReaderIntent.NarrationStateChanged(
                NarrationState.Playing(unlockedStory.id, sentenceIndex = 0, totalSentences = 3),
            ),
        ).state
        assertEquals(ListenPillStatus.PLAYING, playing.listenPillStatus)

        val paused = playing.reduce(
            ReaderIntent.NarrationStateChanged(
                NarrationState.Paused(unlockedStory.id, sentenceIndex = 0, totalSentences = 3),
            ),
        ).state
        assertEquals(ListenPillStatus.PAUSED, paused.listenPillStatus)
    }

    @Test
    fun theSpokenWordIsResolvedToCharactersOfItsParagraph() {
        val state = readyStateNarrating(
            NarrationState.Playing(
                unlockedStory.id,
                sentenceIndex = 1,
                totalSentences = 3,
                spokenRange = SpokenRange(0, 6),
            ),
        )

        assertEquals(
            NarrationHighlight(paragraphIndex = 1, start = 0, end = 6),
            state.narrationHighlight,
        )
    }

    @Test
    fun withoutAWordRangeNothingIsHighlighted() {
        val state = readyStateNarrating(
            NarrationState.Playing(unlockedStory.id, sentenceIndex = 2, totalSentences = 3),
        )

        assertNull(state.narrationHighlight)
    }

    @Test
    fun aRangeReachingPastTheSentenceStopsAtItsEnd() {
        val state = readyStateNarrating(
            NarrationState.Playing(
                unlockedStory.id,
                sentenceIndex = 0,
                totalSentences = 3,
                spokenRange = SpokenRange(0, 99),
            ),
        )

        assertEquals(
            NarrationHighlight(paragraphIndex = 0, start = 0, end = 6),
            state.narrationHighlight,
        )
    }

    @Test
    fun aPausedStoryKeepsItsHighlight() {
        val state = readyStateNarrating(
            NarrationState.Paused(
                unlockedStory.id,
                sentenceIndex = 1,
                totalSentences = 3,
                spokenRange = SpokenRange(0, 6),
            ),
        )

        assertEquals(
            NarrationHighlight(paragraphIndex = 1, start = 0, end = 6),
            state.narrationHighlight,
        )
    }

    @Test
    fun nothingIsHighlightedWithoutNarration() {
        assertNull(readyStateNarrating(NarrationState.Idle).narrationHighlight)
    }

    @Test
    fun aPositionPastTheDeliveredTextHighlightsNothing() {
        val state = readyStateNarrating(
            NarrationState.Playing(
                unlockedStory.id,
                sentenceIndex = 9,
                totalSentences = 12,
                spokenRange = SpokenRange(0, 3),
            ),
        )

        assertNull(state.narrationHighlight)
    }

    @Test
    fun listeningProgressCountsTheWordsSpokenSoFar() {
        val state = readyStateNarrating(
            NarrationState.Playing(
                unlockedStory.id,
                sentenceIndex = 1,
                totalSentences = 3,
                spokenRange = SpokenRange(0, 7),
            ),
        )

        assertEquals(2f / 3f, state.narrationProgress, 0.001f)
    }

    @Test
    fun listeningProgressIsZeroBeforeNarrationStarts() {
        val ready = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess))).state

        assertEquals(0f, ready.narrationProgress, 0.001f)
    }

    @Test
    fun listeningProgressReachesOneOnTheFinalWord() {
        val state = readyStateNarrating(
            NarrationState.Playing(
                unlockedStory.id,
                sentenceIndex = 2,
                totalSentences = 3,
                spokenRange = SpokenRange(0, 6),
            ),
        )

        assertEquals(1f, state.narrationProgress, 0.001f)
    }

    @Test
    fun listeningProgressIgnoresScrolling() {
        val scrolled = readyStateNarrating(NarrationState.Idle)
            .reduce(ReaderIntent.ScrollProgressChanged(80)).state

        assertEquals(80, scrolled.progressPercent)
        assertEquals(0f, scrolled.narrationProgress, 0.001f)
    }

    @Test
    fun thePillFollowsTheScrollUntilThereIsAVoiceToFollow() {
        val scrolled = readyStateNarrating(NarrationState.Idle)
            .reduce(ReaderIntent.ScrollProgressChanged(80)).state

        assertEquals(0.8f, scrolled.listenPillProgress, 0.001f)
    }

    @Test
    fun thePillFollowsTheVoiceWhileNarrationIsLive() {
        val scrolled = readyStateNarrating(NarrationState.Idle)
            .reduce(ReaderIntent.ScrollProgressChanged(80)).state

        val listening = scrolled.reduce(
            ReaderIntent.NarrationStateChanged(
                NarrationState.Playing(
                    unlockedStory.id,
                    sentenceIndex = 1,
                    totalSentences = 3,
                    spokenRange = SpokenRange(0, 7),
                ),
            ),
        ).state

        assertEquals(2f / 3f, listening.listenPillProgress, 0.001f)
    }

    @Test
    fun aPausedStoryStillCountsAsListening() {
        val paused = readyStateNarrating(
            NarrationState.Paused(
                unlockedStory.id,
                sentenceIndex = 1,
                totalSentences = 3,
                spokenRange = SpokenRange(0, 7),
            ),
        ).reduce(ReaderIntent.ScrollProgressChanged(10)).state

        assertEquals(2f / 3f, paused.listenPillProgress, 0.001f)
    }

    @Test
    fun aFreshlyCreatedStateIsAlwaysIdle() {
        assertEquals(NarrationState.Idle, initialState().narration)
        assertEquals(ListenPillStatus.IDLE, initialState().listenPillStatus)
    }
}

private fun readyStateNarrating(narration: NarrationState): ReaderUiState =
    initialState()
        .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess))).state
        .copy(narration = narration)
