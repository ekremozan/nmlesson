package com.example.nativeminds.feature.reader.ui.model

import com.example.nativeminds.domain.narration.StorySentence

/**
 * The text the reader is allowed to render, and nothing else.
 *
 * [isTruncated] is the only thing the body composable needs in order to draw the fade — it never
 * receives the withheld paragraphs, so there is no way for them to reach the screen, a text
 * selection, or an accessibility service.
 *
 * [sentences] is the same segmentation the narrator speaks, resolved once by the mapper rather
 * than recomputed per recomposition, and it is what turns a narration position into a character
 * range this body can highlight. [wordTotals] does the same for the listening progress bar: one
 * running total per sentence boundary, so a spoken position becomes a fraction of the story
 * without walking the text again on every word.
 */
data class ReaderBodyUiModel(
    val paragraphs: List<String>,
    val sentences: List<StorySentence>,
    val wordTotals: List<Int>,
    val isTruncated: Boolean,
    val freeSharePercent: Int,
)
