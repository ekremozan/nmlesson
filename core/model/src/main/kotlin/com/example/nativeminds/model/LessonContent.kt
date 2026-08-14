package com.example.nativeminds.model

/**
 * The readable payload of a [Lesson] — what the reader screen renders and the lesson list never
 * needs.
 *
 * Paragraphs arrive already split and trimmed, so nothing above this layer parses text: the
 * mapper in `:core:data` owns the storage format, and everything else sees a list.
 */
data class LessonContent(
    val lessonId: Long,
    val author: String,
    val paragraphs: List<String>,
)
