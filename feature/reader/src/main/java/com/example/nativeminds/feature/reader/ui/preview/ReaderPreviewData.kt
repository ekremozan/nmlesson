package com.example.nativeminds.feature.reader.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.feature.reader.ui.ReaderContentUiState
import com.example.nativeminds.feature.reader.ui.ReaderUiState
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderStoryUiModel

/**
 * Fixtures for every reader preview.
 *
 * Hand-written rather than pulled from the seed or a ViewModel: a preview has to render with no
 * dependencies at all, and tying it to real data would make it fail for reasons that have nothing
 * to do with the composable being previewed.
 */
val PreviewFreeStory = ReaderStoryUiModel(
    id = 1,
    category = "Fiction",
    title = "The Lighthouse Keeper's Last Letter",
    author = "Marguerite Halloran",
    minutes = 6,
    hasAudio = true,
    isPremium = false,
)

val PreviewPremiumStory = ReaderStoryUiModel(
    id = 3,
    category = "History",
    title = "The Cartographer of Missing Islands",
    author = "Tomás Ferreiro",
    minutes = 8,
    hasAudio = false,
    isPremium = true,
)

val PreviewFullBody = ReaderBodyUiModel(
    paragraphs = listOf(
        "Forty winters he kept the log, and forty winters the log kept him. Wind from the northwest, it said. Swell moderate. Nothing to report. The sentences were short because the weather was long, and because a man who writes too much about the sea begins to argue with it.",
        "The keeper's handwriting changed twice in those years. Once in 1931, when the light was converted and the great brass clockwork he had wound every four hours was carried down the stairs in pieces. Once in 1948, for reasons the log does not give.",
        "Between the pages of the final volume, folded into eighths and soft as cloth, the inspectors found a letter. It was addressed to a house in Galway that had been empty for a decade.",
    ),
    isTruncated = false,
    freeSharePercent = 100,
)

val PreviewTruncatedBody = ReaderBodyUiModel(
    paragraphs = listOf(
        "For ninety-one years, the Admiralty chart showed an island at 44° south. Ships were told to give it a wide berth. Whalers reported its cliffs in fog. A cable company planned a station on it. There was no island.",
        "The error began, as most durable errors do, with a careful man. In 1826 a sealing captain named Ross recorded land where he had seen a bank of low cloud, and because his other measurements were unusually good, the mistake inherited his credibility.",
    ),
    isTruncated = true,
    freeSharePercent = 30,
)

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
                storyId = PreviewFreeStory.id,
                content = ReaderContentUiState.Ready(PreviewFreeStory, PreviewFullBody),
                progressPercent = 34,
            ),
        ),
        ReaderPreviewCase(
            label = "Restricted",
            state = ReaderUiState(
                storyId = PreviewPremiumStory.id,
                content = ReaderContentUiState.Ready(PreviewPremiumStory, PreviewTruncatedBody),
            ),
        ),
        ReaderPreviewCase(
            label = "Loading",
            state = ReaderUiState(storyId = PreviewFreeStory.id),
        ),
        ReaderPreviewCase(
            label = "Offline",
            state = ReaderUiState(
                storyId = PreviewFreeStory.id,
                content = ReaderContentUiState.Unavailable(UnavailableReason.OFFLINE),
            ),
        ),
        ReaderPreviewCase(
            label = "Story missing",
            state = ReaderUiState(
                storyId = PreviewFreeStory.id,
                content = ReaderContentUiState.Unavailable(UnavailableReason.STORY_MISSING),
            ),
        ),
    )
}

/** The three unavailable reasons, for the state composable's own preview. */
class UnavailableReasons : PreviewParameterProvider<UnavailableReason> {
    override val values = UnavailableReason.entries.asSequence()
}
