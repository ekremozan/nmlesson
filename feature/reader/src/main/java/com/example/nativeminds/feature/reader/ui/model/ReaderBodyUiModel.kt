package com.example.nativeminds.feature.reader.ui.model

/**
 * The text the reader is allowed to render, and nothing else.
 *
 * [isTruncated] is the only thing the body composable needs in order to draw the fade — it never
 * receives the withheld paragraphs, so there is no way for them to reach the screen, a text
 * selection, or an accessibility service.
 */
data class ReaderBodyUiModel(
    val paragraphs: List<String>,
    val isTruncated: Boolean,
    val freeSharePercent: Int,
)
