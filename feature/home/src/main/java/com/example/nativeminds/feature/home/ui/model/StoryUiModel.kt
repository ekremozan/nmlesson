package com.example.nativeminds.feature.home.ui.model

/**
 * Compose-facing shape of a story — pre-formatted, never a [com.example.nativeminds.model.Story].
 */
data class StoryUiModel(
    val id: Long,
    val category: String,
    val title: String,
    val teaser: String,
    val minutesLabel: String,
    val hasAudio: Boolean,
    val isLocked: Boolean,
)
