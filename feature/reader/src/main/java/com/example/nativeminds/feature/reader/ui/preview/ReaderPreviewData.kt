package com.example.nativeminds.feature.reader.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.SpokenRange
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.domain.narration.sentenceWordTotals
import com.example.nativeminds.domain.narration.toLessonSentences
import com.example.nativeminds.feature.reader.ui.ReaderContentUiState
import com.example.nativeminds.feature.reader.ui.ReaderUiState
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderLessonUiModel

/**
 * Fixtures for every reader preview.
 *
 * Hand-written rather than pulled from the seed or a ViewModel: a preview has to render with no
 * dependencies at all, and tying it to real data would make it fail for reasons that have nothing
 * to do with the composable being previewed.
 */
val PreviewFreeLesson = ReaderLessonUiModel(
    id = 1,
    subject = "Biyoloji",
    title = "Hücre Yapısı ve Organeller",
    author = "Dr. Elif Kaya",
    minutes = 6,
    hasAudio = true,
    isPremium = false,
    image = "subject_biology",
)

val PreviewPremiumLesson = ReaderLessonUiModel(
    id = 3,
    subject = "Tarih",
    title = "İstanbul'un Fethi ve Sonuçları",
    author = "Doç. Dr. Mehmet Aydın",
    minutes = 8,
    hasAudio = false,
    isPremium = true,
    image = "subject_history",
)

private val PreviewFullParagraphs = listOf(
    "Forty winters he kept the log, and forty winters the log kept him. Wind from the northwest, it said. Swell moderate. Nothing to report. The sentences were short because the weather was long, and because a man who writes too much about the sea begins to argue with it.",
    "The keeper's handwriting changed twice in those years. Once in 1931, when the light was converted and the great brass clockwork he had wound every four hours was carried down the stairs in pieces. Once in 1948, for reasons the log does not give.",
    "Between the pages of the final volume, folded into eighths and soft as cloth, the inspectors found a letter. It was addressed to a house in Galway that had been empty for a decade.",
)

val PreviewFullBody = ReaderBodyUiModel(
    paragraphs = PreviewFullParagraphs,
    sentences = PreviewFullParagraphs.toLessonSentences(),
    wordTotals = PreviewFullParagraphs.sentenceWordTotals(),
    isTruncated = false,
    freeSharePercent = 100,
)

private val PreviewTruncatedParagraphs = listOf(
    "For ninety-one years, the Admiralty chart showed an island at 44° south. Ships were told to give it a wide berth. Whalers reported its cliffs in fog. A cable company planned a station on it. There was no island.",
    "The error began, as most durable errors do, with a careful man. In 1826 a sealing captain named Ross recorded land where he had seen a bank of low cloud, and because his other measurements were unusually good, the mistake inherited his credibility.",
)

val PreviewTruncatedBody = ReaderBodyUiModel(
    paragraphs = PreviewTruncatedParagraphs,
    sentences = PreviewTruncatedParagraphs.toLessonSentences(),
    wordTotals = PreviewTruncatedParagraphs.sentenceWordTotals(),
    isTruncated = true,
    freeSharePercent = 30,
)

private fun spokenWordRange(sentenceIndex: Int, word: String): SpokenRange {
    val sentence = PreviewFullBody.sentences[sentenceIndex]
    val sentenceText = PreviewFullParagraphs[sentence.paragraphIndex]
        .substring(sentence.start, sentence.end)
    val start = sentenceText.indexOf(word)
    return SpokenRange(start, start + word.length)
}

/** One reader state, labelled so a preview row says what it is showing. */
data class ReaderPreviewCase(val label: String, val state: ReaderUiState)

/**
 * Every state the reader can be in, rendered from one `@Composable`.
 *
 * Themes come from the multipreview annotation and states come from here, so the grid is never
 * hand-written as N copies of `@Preview`.
 */
class ReaderPreviewCases : PreviewParameterProvider<ReaderPreviewCase> {
    override val values = sequenceOf(
        ReaderPreviewCase(
            label = "Full access",
            state = ReaderUiState(
                lessonId = PreviewFreeLesson.id,
                content = ReaderContentUiState.Ready(PreviewFreeLesson, PreviewFullBody),
                progressPercent = 34,
            ),
        ),
        ReaderPreviewCase(
            label = "Listening",
            state = ReaderUiState(
                lessonId = PreviewFreeLesson.id,
                content = ReaderContentUiState.Ready(PreviewFreeLesson, PreviewFullBody),
                progressPercent = 34,
                narration = NarrationState.Playing(
                    lessonId = PreviewFreeLesson.id,
                    sentenceIndex = 4,
                    totalSentences = 12,
                    spokenRange = spokenWordRange(sentenceIndex = 4, word = "weather"),
                ),
            ),
        ),
        ReaderPreviewCase(
            label = "Listening without word ranges",
            state = ReaderUiState(
                lessonId = PreviewFreeLesson.id,
                content = ReaderContentUiState.Ready(PreviewFreeLesson, PreviewFullBody),
                progressPercent = 34,
                narration = NarrationState.Playing(
                    lessonId = PreviewFreeLesson.id,
                    sentenceIndex = 4,
                    totalSentences = 12,
                ),
            ),
        ),
        ReaderPreviewCase(
            label = "Restricted",
            state = ReaderUiState(
                lessonId = PreviewPremiumLesson.id,
                content = ReaderContentUiState.Ready(PreviewPremiumLesson, PreviewTruncatedBody),
            ),
        ),
        ReaderPreviewCase(
            label = "Loading",
            state = ReaderUiState(lessonId = PreviewFreeLesson.id),
        ),
        ReaderPreviewCase(
            label = "Offline",
            state = ReaderUiState(
                lessonId = PreviewFreeLesson.id,
                content = ReaderContentUiState.Unavailable(UnavailableReason.OFFLINE),
            ),
        ),
        ReaderPreviewCase(
            label = "Lesson missing",
            state = ReaderUiState(
                lessonId = PreviewFreeLesson.id,
                content = ReaderContentUiState.Unavailable(UnavailableReason.LESSON_MISSING),
            ),
        ),
    )
}

/** The three unavailable reasons, for the state composable's own preview. */
class UnavailableReasons : PreviewParameterProvider<UnavailableReason> {
    override val values = UnavailableReason.entries.asSequence()
}
