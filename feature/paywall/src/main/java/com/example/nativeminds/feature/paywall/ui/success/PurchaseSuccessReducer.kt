package com.example.nativeminds.feature.paywall.ui.success

/** The only thing in this screen that writes state — pure, same shape as the other reducers. */
fun PurchaseSuccessUiState.reduce(intent: PurchaseSuccessIntent): PurchaseSuccessUiState =
    when (intent) {
        is PurchaseSuccessIntent.StoryChanged -> copy(story = intent.story)
    }
