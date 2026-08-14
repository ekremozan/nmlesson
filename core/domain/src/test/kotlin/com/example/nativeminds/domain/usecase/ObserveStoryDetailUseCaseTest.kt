package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.FakeEntitlementRepository
import com.example.nativeminds.domain.FakeStoryRepository
import com.example.nativeminds.domain.RecordingErrorReporter
import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal val unlockedStory = Story(
    id = 1,
    category = "Fiction",
    title = "The Lighthouse Keeper's Last Letter",
    teaser = "One page he never sent.",
    minutes = 6,
    hasAudio = true,
    isLocked = false,
)

internal val lockedStory = unlockedStory.copy(
    id = 3,
    title = "The Cartographer of Missing Islands",
    isLocked = true,
)

internal val storyContent = StoryContent(
    storyId = 1,
    author = "Marguerite Halloran",
    paragraphs = listOf("a".repeat(30), "b".repeat(30), "c".repeat(30), "d".repeat(30)),
)

class ObserveStoryDetailUseCaseTest {
    private val entitlements = FakeEntitlementRepository()
    private val errorReporter = RecordingErrorReporter()

    private fun useCase(repository: FakeStoryRepository) = ObserveStoryDetailUseCase(
        storyRepository = repository,
        entitlementRepository = entitlements,
        refreshStoryContent = RefreshStoryContentUseCase(repository, errorReporter),
    )

    @Test
    fun anUnlockedStoryIsFullyReadableWithoutAnEntitlement() = runTest {
        val repository = FakeStoryRepository(unlockedStory, storyContent)

        val detail = useCase(repository).invoke(unlockedStory.id).first()

        val access = (detail as ReaderDetail.Available).access
        assertTrue(access is ReaderAccess.Full)
        assertEquals(storyContent.paragraphs, access.paragraphs)
    }

    @Test
    fun aPremiumStoryIsFullyReadableWithAnEntitlement() = runTest {
        val repository = FakeStoryRepository(lockedStory, storyContent)
        entitlements.setPremium(true)

        val detail = useCase(repository).invoke(lockedStory.id).first()

        assertTrue((detail as ReaderDetail.Available).access is ReaderAccess.Full)
    }

    @Test
    fun aPremiumStoryWithoutAnEntitlementIsOnlyAPreview() = runTest {
        val repository = FakeStoryRepository(lockedStory, storyContent)

        val detail = useCase(repository).invoke(lockedStory.id).first()

        val access = (detail as ReaderDetail.Available).access
        assertTrue(access is ReaderAccess.Preview)
        assertEquals(FREE_SHARE_PERCENT, (access as ReaderAccess.Preview).freeSharePercent)
    }

    @Test
    fun aPreviewIsAStrictPrefixAndNeverTheWholeStory() = runTest {
        val repository = FakeStoryRepository(lockedStory, storyContent)

        val detail = useCase(repository).invoke(lockedStory.id).first()

        val shown = (detail as ReaderDetail.Available).access.paragraphs
        assertEquals(storyContent.paragraphs.take(shown.size), shown)
        assertNotEquals(storyContent.paragraphs, shown)
    }

    @Test
    fun gainingAnEntitlementUnlocksAnOpenStory() = runTest {
        val repository = FakeStoryRepository(lockedStory, storyContent)
        val details = useCase(repository).invoke(lockedStory.id)

        assertTrue((details.first() as ReaderDetail.Available).access is ReaderAccess.Preview)

        entitlements.setPremium(true)

        val unlocked = details.first { (it as ReaderDetail.Available).access is ReaderAccess.Full }
        assertTrue((unlocked as ReaderDetail.Available).access is ReaderAccess.Full)
    }

    @Test
    fun aStoryThatIsNoLongerStoredIsUnavailable() = runTest {
        val repository = FakeStoryRepository(story = null, content = storyContent)

        val detail = useCase(repository).invoke(unlockedStory.id).first()

        assertEquals(ReaderDetail.Unavailable(UnavailableReason.STORY_MISSING), detail)
    }

    @Test
    fun missingTextWhileOfflineIsReportedAsOffline() = runTest {
        val repository = FakeStoryRepository(unlockedStory, content = null)
        repository.refreshFailure = OfflineException("no network")

        val detail = useCase(repository).invoke(unlockedStory.id)
            .first { it is ReaderDetail.Unavailable }

        assertEquals(ReaderDetail.Unavailable(UnavailableReason.OFFLINE), detail)
        assertEquals(1, errorReporter.reported.size)
    }

    @Test
    fun aFailedFetchIsReportedAsAnError() = runTest {
        val repository = FakeStoryRepository(unlockedStory, content = null)
        repository.refreshFailure = IllegalStateException("boom")

        val detail = useCase(repository).invoke(unlockedStory.id)
            .first { it is ReaderDetail.Unavailable }

        assertEquals(ReaderDetail.Unavailable(UnavailableReason.ERROR), detail)
        assertEquals(1, errorReporter.reported.size)
    }

    @Test
    fun missingTextStartsAFetchBeforeGivingUp() = runTest {
        val repository = FakeStoryRepository(unlockedStory, content = null)
        repository.refreshFailure = OfflineException("no network")

        useCase(repository).invoke(unlockedStory.id).first { it is ReaderDetail.Unavailable }

        assertEquals(1, repository.refreshCount)
    }
}
