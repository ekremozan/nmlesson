package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.UnavailableReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun restrictedContentOpensTheUnlockSheet() {
        val reduction = initialState().reduce(
            ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess)),
        )

        assertTrue(reduction.state.isRestricted)
        assertTrue(reduction.state.isUnlockSheetVisible)
    }

    @Test
    fun aDismissedSheetIsNotReopenedByTheSameStoryArrivingAgain() {
        val dismissed = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess))).state
            .reduce(ReaderIntent.UnlockSheetDismissed).state

        val reopened = dismissed
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess))).state

        assertFalse(reopened.isUnlockSheetVisible)
        assertTrue(reopened.showUnlockAffordance)
    }

    @Test
    fun theReaderCanAskForTheSheetBack() {
        val dismissed = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess))).state
            .reduce(ReaderIntent.UnlockSheetDismissed).state

        val requested = dismissed.reduce(ReaderIntent.UnlockSheetRequested).state

        assertTrue(requested.isUnlockSheetVisible)
    }

    @Test
    fun gainingEntitlementClearsTheRestriction() {
        val restricted = initialState()
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(previewAccess))).state

        val unlocked = restricted
            .reduce(ReaderIntent.DetailChanged(ReaderDetail.Available(fullAccess))).state

        assertFalse(unlocked.isRestricted)
        assertFalse(unlocked.isUnlockSheetVisible)
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
    fun subscribingReportsThatItIsUnavailableAndChangesNothing() {
        val state = initialState()

        val reduction = state.reduce(ReaderIntent.SubscribeClicked)

        assertEquals(state, reduction.state)
        assertEquals(listOf(ReaderEffect.ShowSubscriptionUnavailable), reduction.effects)
    }

    @Test
    fun listeningReportsThatItIsUnavailableAndChangesNothing() {
        val state = initialState()

        val reduction = state.reduce(ReaderIntent.ListenClicked)

        assertEquals(state, reduction.state)
        assertEquals(listOf(ReaderEffect.ShowAudioUnavailable), reduction.effects)
    }
}
