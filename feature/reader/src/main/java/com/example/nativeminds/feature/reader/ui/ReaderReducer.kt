package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.feature.reader.ui.mapper.toBodyUiModel
import com.example.nativeminds.feature.reader.ui.mapper.toStoryUiModel

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

    ReaderIntent.ListenClicked -> Reduction(this, listOf(ReaderEffect.ShowAudioUnavailable))
}

private fun ReaderUiState.reduceDetail(detail: ReaderDetail): Reduction = when (detail) {
    ReaderDetail.Loading -> Reduction(copy(content = ReaderContentUiState.Loading))

    is ReaderDetail.Unavailable -> Reduction(
        copy(content = ReaderContentUiState.Unavailable(detail.reason)),
    )

    is ReaderDetail.Available -> {
        val body = detail.access.toBodyUiModel()
        Reduction(
            copy(content = ReaderContentUiState.Ready(detail.access.toStoryUiModel(), body)),
        )
    }
}
