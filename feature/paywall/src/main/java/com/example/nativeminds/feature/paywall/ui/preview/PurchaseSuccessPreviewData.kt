package com.example.nativeminds.feature.paywall.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.feature.paywall.ui.success.PurchaseSuccessUiState
import com.example.nativeminds.model.Story

val PreviewResumeStory = Story(
    id = 3,
    category = "History",
    title = "The Cartographer of Missing Islands",
    teaser = "One page he never sent.",
    minutes = 8,
    hasAudio = false,
    isLocked = true,
    image = "cover_03",
)

data class PurchaseSuccessPreviewCase(val label: String, val state: PurchaseSuccessUiState)

class PurchaseSuccessPreviewCases : PreviewParameterProvider<PurchaseSuccessPreviewCase> {
    override val values = sequenceOf(
        PurchaseSuccessPreviewCase(
            label = "Story loaded",
            state = PurchaseSuccessUiState(
                storyId = PreviewResumeStory.id,
                progressPercent = 30,
                plan = PurchasePlan.YEARLY,
                story = PreviewResumeStory,
            ),
        ),
        PurchaseSuccessPreviewCase(
            label = "Loading",
            state = PurchaseSuccessUiState(
                storyId = PreviewResumeStory.id,
                progressPercent = 30,
                plan = PurchasePlan.MONTHLY,
            ),
        ),
    )
}
