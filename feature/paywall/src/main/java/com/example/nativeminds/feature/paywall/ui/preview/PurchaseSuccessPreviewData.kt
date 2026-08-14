package com.example.nativeminds.feature.paywall.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.feature.paywall.ui.success.PurchaseSuccessUiState
import com.example.nativeminds.model.Lesson

val PreviewResumeLesson = Lesson(
    id = 3,
    subject = "Tarih",
    title = "İstanbul'un Fethi ve Sonuçları",
    teaser = "Bir çağın kapanıp diğerinin açılması.",
    minutes = 8,
    hasAudio = false,
    isLocked = true,
    image = "subject_history",
)

data class PurchaseSuccessPreviewCase(val label: String, val state: PurchaseSuccessUiState)

class PurchaseSuccessPreviewCases : PreviewParameterProvider<PurchaseSuccessPreviewCase> {
    override val values = sequenceOf(
        PurchaseSuccessPreviewCase(
            label = "Lesson loaded",
            state = PurchaseSuccessUiState(
                lessonId = PreviewResumeLesson.id,
                progressPercent = 30,
                plan = PurchasePlan.YEARLY,
                lesson = PreviewResumeLesson,
            ),
        ),
        PurchaseSuccessPreviewCase(
            label = "Loading",
            state = PurchaseSuccessUiState(
                lessonId = PreviewResumeLesson.id,
                progressPercent = 30,
                plan = PurchasePlan.MONTHLY,
            ),
        ),
    )
}
