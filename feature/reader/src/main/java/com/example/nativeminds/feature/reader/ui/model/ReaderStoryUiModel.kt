package com.example.nativeminds.feature.reader.ui.model

/**
 * Compose-facing header data for the reader — pre-formatted, never a
 * [com.example.nativeminds.model.Story].
 */
data class ReaderStoryUiModel(
    val id: Long,
    val category: String,
    val title: String,
    val author: String,
    val minutes: Int,
    val hasAudio: Boolean,
    val isPremium: Boolean,
    val image: String,
)
