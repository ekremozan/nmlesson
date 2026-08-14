package com.example.nativeminds.feature.paywall.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.paywall.ui.paywall.PaywallUiState
import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan

/** One paywall state, labelled so a preview row says what it is showing. */
data class PaywallPreviewCase(val label: String, val state: PaywallUiState)

class PaywallPreviewCases : PreviewParameterProvider<PaywallPreviewCase> {
    override val values = sequenceOf(
        PaywallPreviewCase(
            label = "Monthly selected",
            state = PaywallUiState(lessonId = 3, progressPercent = 30),
        ),
        PaywallPreviewCase(
            label = "Yearly selected",
            state = PaywallUiState(
                lessonId = 3,
                progressPercent = 30,
                selectedPlan = PurchasePlan.YEARLY,
            ),
        ),
    )
}
