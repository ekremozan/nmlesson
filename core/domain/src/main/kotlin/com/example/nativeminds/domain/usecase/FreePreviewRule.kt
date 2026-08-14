package com.example.nativeminds.domain.usecase

/** How much of a premium lesson a reader without an entitlement gets to read. */
const val FREE_SHARE_PERCENT = 30

private const val PERCENT = 100

/**
 * The opening share of a lesson, cut on a paragraph boundary.
 *
 * Whole paragraphs rather than an exact character count: the fade has to land on a natural break,
 * and a cut mid-word reads as a bug rather than as a boundary. The consequence is that a lesson
 * whose first paragraph is longer than the share gives away a little more — bounded by one
 * paragraph, and the better trade.
 *
 * Never returns everything: a restricted lesson that returned its whole body would silently stop
 * being restricted.
 */
fun freePreview(paragraphs: List<String>, sharePercent: Int = FREE_SHARE_PERCENT): List<String> {
    if (paragraphs.size <= 1) return paragraphs

    val budget = paragraphs.sumOf { it.length } * sharePercent / PERCENT
    var used = 0
    val preview = mutableListOf<String>()

    for (paragraph in paragraphs) {
        preview += paragraph
        used += paragraph.length
        if (used >= budget) break
    }

    return if (preview.size == paragraphs.size) preview.dropLast(1) else preview
}
