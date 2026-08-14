package com.example.nativeminds.data.local

import com.example.nativeminds.model.LessonContent

/**
 * The text behind each lesson in [DummyLessonSeed], keyed by the same position-derived ids — see
 * [LessonSeedCatalog] for where the paragraphs actually live.
 *
 * Kept in its own file rather than inlined into the lesson list: the metadata list is scanned by
 * anyone changing the catalog, and burying full lesson text inside it would make that unreadable.
 *
 * Cut corner: hand-written content standing in for a real catalog until a backend exists.
 */
object DummyLessonContentSeed {
    val content: List<LessonContent> = LessonSeedCatalog.all.mapIndexed { index, topic ->
        LessonContent(
            lessonId = (index + 1).toLong(),
            author = topic.author,
            paragraphs = topic.paragraphs,
        )
    }
}
