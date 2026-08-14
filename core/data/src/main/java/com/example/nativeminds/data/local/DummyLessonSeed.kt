package com.example.nativeminds.data.local

import com.example.nativeminds.model.Lesson

private val SUBJECT_IMAGE_KEYS = mapOf(
    "Biyoloji" to "subject_biology",
    "Tarih" to "subject_history",
    "Coğrafya" to "subject_geography",
    "Kimya" to "subject_chemistry",
)

/**
 * Bootstrap content written to Room the first time the app runs with an empty database, so
 * offline-first holds even before the first successful sync — see
 * [com.example.nativeminds.data.RoomLessonRepository.syncIfNeeded].
 *
 * One row per [LessonSeedCatalog.all] entry — a real 1:1 mapping between topic and content, unlike
 * the old fiction seed's title-variant fan-out. The image is derived entirely from [Lesson.subject]
 * ([SUBJECT_IMAGE_KEYS]) rather than authored per row: every topic in a subject shares that
 * subject's one representative illustration.
 *
 * Cut corner: hand-seeded content standing in for a real catalog until a backend exists.
 */
object DummyLessonSeed {
    val lessons: List<Lesson> = LessonSeedCatalog.all.mapIndexed { index, topic ->
        Lesson(
            id = (index + 1).toLong(),
            subject = topic.subject,
            title = topic.title,
            teaser = topic.teaser,
            minutes = topic.minutes,
            hasAudio = topic.hasAudio,
            isLocked = topic.isLocked,
            image = SUBJECT_IMAGE_KEYS.getValue(topic.subject),
        )
    }
}
