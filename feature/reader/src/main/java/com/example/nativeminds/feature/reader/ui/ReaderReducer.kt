package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.feature.reader.ui.mapper.toBodyUiModel
import com.example.nativeminds.feature.reader.ui.mapper.toLessonUiModel

private const val MIN_PROGRESS = 0
private const val MAX_PROGRESS = 100

/**
 * The only thing in this feature that writes state.
 *
 * Pure and top-level on purpose: no Android, no coroutines, no ViewModel, so every transition on
 * this screen — including the ones that arrive from Room rather than from a finger — is provable
 * in a plain JVM test.
 */
fun ReaderUiState.reduce(intent: ReaderIntent): Reduction = when (intent) {
    is ReaderIntent.DetailChanged -> reduceDetail(intent.detail)

    ReaderIntent.RetryRequested -> Reduction(
        copy(content = ReaderContentUiState.Loading, retryToken = retryToken + 1),
    )

    is ReaderIntent.ScrollProgressChanged -> Reduction(
        copy(progressPercent = intent.percent.coerceIn(MIN_PROGRESS, MAX_PROGRESS)),
    )

    ReaderIntent.ListenClicked -> reduceListenClicked()

    is ReaderIntent.NarrationStateChanged -> Reduction(copy(narration = intent.narration))
}

/**
 * What tapping the pill means depends on what narration is already doing — the toggle behavior
 * pause/resume/leave-and-return requires lives entirely in this one branch.
 */
private fun ReaderUiState.reduceListenClicked(): Reduction {
    val body = readyContent?.body ?: return Reduction(this)
    return when (narration) {
        NarrationState.Idle -> Reduction(this, listOf(ReaderEffect.StartNarration(body.paragraphs)))
        is NarrationState.Playing -> Reduction(this, listOf(ReaderEffect.PauseNarration))
        is NarrationState.Paused -> Reduction(this, listOf(ReaderEffect.ResumeNarration))
        is NarrationState.Unavailable -> Reduction(this, listOf(ReaderEffect.ShowAudioUnavailable))
    }
}

private fun ReaderUiState.reduceDetail(detail: ReaderDetail): Reduction = when (detail) {
    ReaderDetail.Loading -> Reduction(copy(content = ReaderContentUiState.Loading))

    is ReaderDetail.Unavailable -> Reduction(
        copy(content = ReaderContentUiState.Unavailable(detail.reason)),
    )

    is ReaderDetail.Available -> {
        val body = detail.access.toBodyUiModel()
        Reduction(
            copy(content = ReaderContentUiState.Ready(detail.access.toLessonUiModel(), body)),
        )
    }
}
