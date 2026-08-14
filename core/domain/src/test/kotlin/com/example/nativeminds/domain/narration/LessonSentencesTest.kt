package com.example.nativeminds.domain.narration

import org.junit.Assert.assertEquals
import org.junit.Test

class LessonSentencesTest {
    @Test
    fun `splits a paragraph into its sentences`() {
        val result = listOf("First sentence. Second sentence! Third?").sentenceTexts()

        assertEquals(listOf("First sentence.", "Second sentence!", "Third?"), result)
    }

    @Test
    fun `treats a paragraph break as a sentence boundary even without punctuation`() {
        val result = listOf("Title", "Body text starts here.").sentenceTexts()

        assertEquals(listOf("Title", "Body text starts here."), result)
    }

    @Test
    fun `drops blank paragraphs`() {
        val result = listOf("", "   ", "Only real sentence.").sentenceTexts()

        assertEquals(listOf("Only real sentence."), result)
    }

    @Test
    fun `returns an empty list for empty input`() {
        assertEquals(emptyList<String>(), listOf<String>().sentenceTexts())
    }

    @Test
    fun `every sentence points at the characters it came from`() {
        val paragraphs = listOf("  First. Second sentence!  ", "Third one.")

        val sentences = paragraphs.toLessonSentences()

        assertEquals(
            listOf("First.", "Second sentence!", "Third one."),
            sentences.map { paragraphs[it.paragraphIndex].substring(it.start, it.end) },
        )
    }

    @Test
    fun `sentences carry the index of the paragraph they belong to`() {
        val sentences = listOf("One. Two.", "Three.").toLessonSentences()

        assertEquals(listOf(0, 0, 1), sentences.map { it.paragraphIndex })
    }

    @Test
    fun `a sentence after leading whitespace starts past it`() {
        val sentences = listOf("A. \t B.").toLessonSentences()

        assertEquals(listOf(LessonSentence(0, 0, 2), LessonSentence(0, 5, 7)), sentences)
    }
}
