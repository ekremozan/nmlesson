package com.example.nativeminds.feature.home.ui.model

/**
 * Compose-facing shape of a lesson — pre-formatted, never a [com.example.nativeminds.model.Lesson].
 */
data class LessonUiModel(
    val id: Long,
    val subject: String,
    val title: String,
    val teaser: String,
    val minutesLabel: String,
    val hasAudio: Boolean,
    val isLocked: Boolean,
    val image: String,
)
