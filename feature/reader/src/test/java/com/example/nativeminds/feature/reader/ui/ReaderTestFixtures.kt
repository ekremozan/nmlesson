package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent

internal val unlockedLesson = Lesson(
    id = 1,
    subject = "Biyoloji",
    title = "Hücre Yapısı ve Organeller",
    teaser = "Zarın içindeki küçük fabrika.",
    minutes = 6,
    hasAudio = true,
    isLocked = false,
    image = "subject_biology",
)

internal val lockedLesson = unlockedLesson.copy(
    id = 3,
    subject = "Tarih",
    title = "İstanbul'un Fethi ve Sonuçları",
    isLocked = true,
)

internal val lessonContent = LessonContent(
    lessonId = unlockedLesson.id,
    author = "Dr. Elif Kaya",
    paragraphs = listOf("First.", "Second.", "Third."),
)

internal val fullAccess = ReaderAccess.Full(unlockedLesson, lessonContent)

internal val previewAccess = ReaderAccess.Preview(
    lesson = lockedLesson,
    author = "Doç. Dr. Mehmet Aydın",
    paragraphs = listOf("First."),
    freeSharePercent = 30,
)

internal fun initialState() = ReaderUiState(lessonId = unlockedLesson.id)
