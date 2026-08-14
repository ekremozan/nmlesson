package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderStoryUiModel

/**
 * Everything the reader screen can be showing.
 *
 * Split out of [ReaderUiState] so the three cases are mutually exclusive by construction — there
 * is no combination of flags that produces "loading and unavailable at once".
 */
sealed interface ReaderContentUiState {
    data object Loading : ReaderContentUiState

    data class Ready(
        val story: ReaderStoryUiModel,
        val body: ReaderBodyUiModel,
    ) : ReaderContentUiState

    data class Unavailable(val reason: UnavailableReason) : ReaderContentUiState
}

/**
 * The reader's state — irreducible facts only. Anything that can be worked out from these is a
 * computed property rather than a field the reducer has to keep in step.
 */
data class ReaderUiState(
    val storyId: Long,
    val content: ReaderContentUiState = ReaderContentUiState.Loading,
    val progressPercent: Int = 0,
    /**
     * Incremented by a retry so the load key changes and the content flow re-subscribes.
     *
     * A count of how many times the reader has asked, which is a fact about the world rather than
     * derived data — it belongs in state precisely so retry stays a pure transition.
     */
    val retryToken: Int = 0,
) {
    val readyContent: ReaderContentUiState.Ready?
        get() = content as? ReaderContentUiState.Ready

    val isRestricted: Boolean
        get() = readyContent?.body?.isTruncated == true
}

sealed interface ReaderIntent {
    /** Content arriving from the domain layer, folded in through the same door as user actions. */
    data class DetailChanged(val detail: ReaderDetail) : ReaderIntent

    data object RetryRequested : ReaderIntent

    data class ScrollProgressChanged(val percent: Int) : ReaderIntent

    data object ListenClicked : ReaderIntent
}

sealed interface ReaderEffect {
    data object ShowAudioUnavailable : ReaderEffect
}

/**
 * What a reduction produces: the next state, and any one-shot effects that intent raised.
 *
 * Effects come back from the reducer rather than being sent by the ViewModel because the mapping
 * from intent to effect is a decision, and the ViewModel is not allowed to make decisions — it
 * would have to branch on the intent to do so. Here the decision stays in the one pure, testable
 * place that already owns every other consequence of an intent.
 */
data class Reduction(
    val state: ReaderUiState,
    val effects: List<ReaderEffect> = emptyList(),
)
