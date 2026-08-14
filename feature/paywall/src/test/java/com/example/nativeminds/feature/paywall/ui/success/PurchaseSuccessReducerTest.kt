package com.example.nativeminds.feature.paywall.ui.success

import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.model.Lesson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val lesson = Lesson(
    id = 3,
    subject = "Tarih",
    title = "İstanbul'un Fethi ve Sonuçları",
    teaser = "Bir çağın kapanıp diğerinin açılması.",
    minutes = 8,
    hasAudio = false,
    isLocked = true,
    image = "subject_history",
)

private fun initialState() =
    PurchaseSuccessUiState(lessonId = lesson.id, progressPercent = 30, plan = PurchasePlan.YEARLY)

class PurchaseSuccessReducerTest {
    @Test
    fun startsWithNoLessonLoaded() {
        assertNull(initialState().lesson)
    }

    @Test
    fun lessonArrivingFillsTheResumeCard() {
        val state = initialState().reduce(PurchaseSuccessIntent.LessonChanged(lesson))

        assertEquals(lesson, state.lesson)
    }

    @Test
    fun theLessonDisappearingClearsTheResumeCard() {
        val withLesson = initialState().reduce(PurchaseSuccessIntent.LessonChanged(lesson))

        val state = withLesson.reduce(PurchaseSuccessIntent.LessonChanged(null))

        assertNull(state.lesson)
    }
}
