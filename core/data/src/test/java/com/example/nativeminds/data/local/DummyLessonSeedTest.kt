package com.example.nativeminds.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val EXPECTED_SUBJECT_COUNT = 4
private const val EXPECTED_TOPICS_PER_SUBJECT = 10
private const val EXPECTED_LESSON_COUNT = EXPECTED_SUBJECT_COUNT * EXPECTED_TOPICS_PER_SUBJECT
private const val EXPECTED_FREE_TOPICS_PER_SUBJECT = 3

class DummyLessonSeedTest {
    @Test
    fun generatesExactlyFortyLessonsWithUniqueIds() {
        val ids = DummyLessonSeed.lessons.map { it.id }
        assertEquals(EXPECTED_LESSON_COUNT, ids.size)
        assertEquals(EXPECTED_LESSON_COUNT, ids.toSet().size)
    }

    @Test
    fun everySubjectHasExactlyTenTopics() {
        val lessonsPerSubject = DummyLessonSeed.lessons.groupingBy { it.subject }.eachCount()
        assertEquals(EXPECTED_SUBJECT_COUNT, lessonsPerSubject.size)
        lessonsPerSubject.values.forEach { count -> assertEquals(EXPECTED_TOPICS_PER_SUBJECT, count) }
    }

    @Test
    fun everySubjectSharesOneImage() {
        val imagesPerSubject = DummyLessonSeed.lessons.groupBy { it.subject }
            .mapValues { (_, lessons) -> lessons.map { it.image }.toSet() }
        imagesPerSubject.values.forEach { images -> assertEquals(1, images.size) }
    }

    @Test
    fun everySubjectHasExactlyThreeFreeTopics() {
        val freePerSubject = DummyLessonSeed.lessons.filter { !it.isLocked }
            .groupingBy { it.subject }.eachCount()
        assertEquals(EXPECTED_SUBJECT_COUNT, freePerSubject.size)
        freePerSubject.values.forEach { count -> assertEquals(EXPECTED_FREE_TOPICS_PER_SUBJECT, count) }
    }

    @Test
    fun everyLessonHasAudio() {
        assertTrue(DummyLessonSeed.lessons.all { it.hasAudio })
    }

    @Test
    fun everyLessonHasMatchingContent() {
        val lessonIds = DummyLessonSeed.lessons.map { it.id }.toSet()
        val contentIds = DummyLessonContentSeed.content.map { it.lessonId }.toSet()
        assertEquals(lessonIds, contentIds)
    }
}
