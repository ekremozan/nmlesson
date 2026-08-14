package com.example.nativeminds.data.local

/**
 * One konu anlatımı ("topic lecture") behind the seed catalog — the single place its Turkish text
 * lives. [LessonSeedCatalog] assigns ids and fans this out into a [com.example.nativeminds.model.Lesson]
 * and its [com.example.nativeminds.model.LessonContent].
 */
internal data class LessonSeedTopic(
    val subject: String,
    val title: String,
    val author: String,
    val teaser: String,
    val minutes: Int,
    val hasAudio: Boolean,
    val isLocked: Boolean,
    val paragraphs: List<String>,
)
