package com.example.nativeminds.feature.reader.ui.mapper

import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.domain.narration.sentenceWordTotals
import com.example.nativeminds.domain.narration.toStorySentences
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderStoryUiModel

fun ReaderAccess.toStoryUiModel(): ReaderStoryUiModel = ReaderStoryUiModel(
    id = story.id,
    category = story.category,
    title = story.title,
    author = author,
    minutes = story.minutes,
    hasAudio = story.hasAudio,
    isPremium = story.isLocked,
    image = story.image,
)

fun ReaderAccess.toBodyUiModel(): ReaderBodyUiModel = ReaderBodyUiModel(
    paragraphs = paragraphs,
    sentences = paragraphs.toStorySentences(),
    wordTotals = paragraphs.sentenceWordTotals(),
    isTruncated = this is ReaderAccess.Preview,
    freeSharePercent = (this as? ReaderAccess.Preview)?.freeSharePercent ?: FULL_SHARE_PERCENT,
)

private const val FULL_SHARE_PERCENT = 100
