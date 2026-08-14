package com.example.nativeminds.feature.reader.ui.mapper

import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.domain.narration.sentenceWordTotals
import com.example.nativeminds.domain.narration.toLessonSentences
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderLessonUiModel

fun ReaderAccess.toLessonUiModel(): ReaderLessonUiModel = ReaderLessonUiModel(
    id = lesson.id,
    subject = lesson.subject,
    title = lesson.title,
    author = author,
    minutes = lesson.minutes,
    hasAudio = lesson.hasAudio,
    isPremium = lesson.isLocked,
    image = lesson.image,
)

fun ReaderAccess.toBodyUiModel(): ReaderBodyUiModel = ReaderBodyUiModel(
    paragraphs = paragraphs,
    sentences = paragraphs.toLessonSentences(),
    wordTotals = paragraphs.sentenceWordTotals(),
    isTruncated = this is ReaderAccess.Preview,
    freeSharePercent = (this as? ReaderAccess.Preview)?.freeSharePercent ?: FULL_SHARE_PERCENT,
)

private const val FULL_SHARE_PERCENT = 100
