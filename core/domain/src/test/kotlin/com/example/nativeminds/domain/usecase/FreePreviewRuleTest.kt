package com.example.nativeminds.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreePreviewRuleTest {
    private val evenParagraphs = List(10) { "x".repeat(100) }

    @Test
    fun thePreviewIsTheShortestWholeParagraphPrefixThatReachesTheShare() {
        val preview = freePreview(evenParagraphs, sharePercent = 30)

        assertEquals(3, preview.size)
        assertEquals(evenParagraphs.take(3), preview)
    }

    @Test
    fun theCutLandsOnAParagraphBoundaryEvenWhenLengthsVary() {
        val paragraphs = listOf("a".repeat(10), "b".repeat(200), "c".repeat(10), "d".repeat(10))

        val preview = freePreview(paragraphs, sharePercent = 30)

        assertEquals(listOf(paragraphs[0], paragraphs[1]), preview)
    }

    @Test
    fun aRestrictedStoryNeverGivesAwayAllOfItself() {
        val preview = freePreview(evenParagraphs, sharePercent = 99)

        assertNotEquals(evenParagraphs, preview)
        assertTrue(preview.size < evenParagraphs.size)
    }

    @Test
    fun aSingleParagraphStoryStillShowsSomething() {
        val paragraphs = listOf("The whole story, in one breath.")

        assertEquals(paragraphs, freePreview(paragraphs, sharePercent = 30))
    }

    @Test
    fun anEmptyStoryStaysEmpty() {
        assertEquals(emptyList<String>(), freePreview(emptyList(), sharePercent = 30))
    }

    @Test
    fun theFirstParagraphIsAlwaysIncludedEvenWhenItExceedsTheShare() {
        val paragraphs = listOf("x".repeat(900), "y".repeat(100))

        assertEquals(listOf(paragraphs[0]), freePreview(paragraphs, sharePercent = 5))
    }
}
