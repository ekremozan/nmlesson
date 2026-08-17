package com.example.nativeminds.data.remote.mapper

import com.example.nativeminds.data.remote.dto.LessonContentDto
import com.example.nativeminds.data.remote.dto.LessonDto
import org.junit.Assert.assertEquals
import org.junit.Test

class LessonDtoMappersTest {
    @Test
    fun lessonDtoMapsFieldForField() {
        val dto = LessonDto(
            id = 1,
            subject = "Biyoloji",
            title = "Hücre Yapısı",
            teaser = "Zarın içindeki küçük fabrika.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            image = "subject_biology",
            language = "tr",
        )

        val domain = dto.toDomain()

        assertEquals(dto.id, domain.id)
        assertEquals(dto.subject, domain.subject)
        assertEquals(dto.title, domain.title)
        assertEquals(dto.teaser, domain.teaser)
        assertEquals(dto.minutes, domain.minutes)
        assertEquals(dto.hasAudio, domain.hasAudio)
        assertEquals(dto.isLocked, domain.isLocked)
        assertEquals(dto.image, domain.image)
    }

    @Test
    fun lessonContentDtoSplitsBodyIntoParagraphs() {
        val dto = LessonContentDto(
            lessonId = 1,
            author = "Doç. Dr. Can Yılmaz",
            body = "First paragraph.\n\nSecond paragraph.",
            language = "tr",
        )

        val domain = dto.toDomain()

        assertEquals(1L, domain.lessonId)
        assertEquals("Doç. Dr. Can Yılmaz", domain.author)
        assertEquals(listOf("First paragraph.", "Second paragraph."), domain.paragraphs)
    }
}
