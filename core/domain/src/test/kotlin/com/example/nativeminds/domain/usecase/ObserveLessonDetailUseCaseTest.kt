package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.FakeEntitlementRepository
import com.example.nativeminds.domain.FakeLessonRepository
import com.example.nativeminds.domain.RecordingErrorReporter
import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal val unlockedLesson = Lesson(
    id = 1,
    subject = "Biyoloji",
    title = "Hücre Yapısı ve Organeller",
    teaser = "Zarın içindeki küçük fabrika.",
    minutes = 6,
    hasAudio = true,
    isLocked = false,
    image = "subject_biology",
)

internal val lockedLesson = unlockedLesson.copy(
    id = 3,
    title = "Kalıtım ve Mendel Genetiği",
    isLocked = true,
)

internal val lessonContent = LessonContent(
    lessonId = 1,
    author = "Marguerite Halloran",
    paragraphs = listOf("a".repeat(30), "b".repeat(30), "c".repeat(30), "d".repeat(30)),
)

class ObserveLessonDetailUseCaseTest {
    private val entitlements = FakeEntitlementRepository()
    private val errorReporter = RecordingErrorReporter()

    private fun useCase(repository: FakeLessonRepository) = ObserveLessonDetailUseCase(
        lessonRepository = repository,
        entitlementRepository = entitlements,
        refreshLessonContent = RefreshLessonContentUseCase(repository, errorReporter),
    )

    @Test
    fun anUnlockedLessonIsFullyReadableWithoutAnEntitlement() = runTest {
        val repository = FakeLessonRepository(unlockedLesson, lessonContent)

        val detail = useCase(repository).invoke(unlockedLesson.id).first()

        val access = (detail as ReaderDetail.Available).access
        assertTrue(access is ReaderAccess.Full)
        assertEquals(lessonContent.paragraphs, access.paragraphs)
    }

    @Test
    fun aPremiumLessonIsFullyReadableWithAnEntitlement() = runTest {
        val repository = FakeLessonRepository(lockedLesson, lessonContent)
        entitlements.setPremium(true)

        val detail = useCase(repository).invoke(lockedLesson.id).first()

        assertTrue((detail as ReaderDetail.Available).access is ReaderAccess.Full)
    }

    @Test
    fun aPremiumLessonWithoutAnEntitlementIsOnlyAPreview() = runTest {
        val repository = FakeLessonRepository(lockedLesson, lessonContent)

        val detail = useCase(repository).invoke(lockedLesson.id).first()

        val access = (detail as ReaderDetail.Available).access
        assertTrue(access is ReaderAccess.Preview)
        assertEquals(FREE_SHARE_PERCENT, (access as ReaderAccess.Preview).freeSharePercent)
    }

    @Test
    fun aPreviewIsAStrictPrefixAndNeverTheWholeLesson() = runTest {
        val repository = FakeLessonRepository(lockedLesson, lessonContent)

        val detail = useCase(repository).invoke(lockedLesson.id).first()

        val shown = (detail as ReaderDetail.Available).access.paragraphs
        assertEquals(lessonContent.paragraphs.take(shown.size), shown)
        assertNotEquals(lessonContent.paragraphs, shown)
    }

    @Test
    fun gainingAnEntitlementUnlocksAnOpenLesson() = runTest {
        val repository = FakeLessonRepository(lockedLesson, lessonContent)
        val details = useCase(repository).invoke(lockedLesson.id)

        assertTrue((details.first() as ReaderDetail.Available).access is ReaderAccess.Preview)

        entitlements.setPremium(true)

        val unlocked = details.first { (it as ReaderDetail.Available).access is ReaderAccess.Full }
        assertTrue((unlocked as ReaderDetail.Available).access is ReaderAccess.Full)
    }

    @Test
    fun aLessonThatIsNoLongerStoredIsUnavailable() = runTest {
        val repository = FakeLessonRepository(lesson = null, content = lessonContent)

        val detail = useCase(repository).invoke(unlockedLesson.id).first()

        assertEquals(ReaderDetail.Unavailable(UnavailableReason.LESSON_MISSING), detail)
    }

    @Test
    fun missingTextWhileOfflineIsReportedAsOffline() = runTest {
        val repository = FakeLessonRepository(unlockedLesson, content = null)
        repository.refreshFailure = OfflineException("no network")

        val detail = useCase(repository).invoke(unlockedLesson.id)
            .first { it is ReaderDetail.Unavailable }

        assertEquals(ReaderDetail.Unavailable(UnavailableReason.OFFLINE), detail)
        assertEquals(1, errorReporter.reported.size)
    }

    @Test
    fun aFailedFetchIsReportedAsAnError() = runTest {
        val repository = FakeLessonRepository(unlockedLesson, content = null)
        repository.refreshFailure = IllegalStateException("boom")

        val detail = useCase(repository).invoke(unlockedLesson.id)
            .first { it is ReaderDetail.Unavailable }

        assertEquals(ReaderDetail.Unavailable(UnavailableReason.ERROR), detail)
        assertEquals(1, errorReporter.reported.size)
    }

    @Test
    fun missingTextStartsAFetchBeforeGivingUp() = runTest {
        val repository = FakeLessonRepository(unlockedLesson, content = null)
        repository.refreshFailure = OfflineException("no network")

        useCase(repository).invoke(unlockedLesson.id).first { it is ReaderDetail.Unavailable }

        assertEquals(1, repository.refreshCount)
    }
}
