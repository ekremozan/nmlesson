package com.example.nativeminds.feature.paywall.ui.success

import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.model.Lesson

/**
 * [lesson] starts `null` while [com.example.nativeminds.domain.repository.LessonRepository.lesson]
 * has not emitted yet — the same "not stored yet" convention the reader already uses, not a
 * distinct loading flag to keep in step.
 */
data class PurchaseSuccessUiState(
    val lessonId: Long,
    val progressPercent: Int,
    val plan: PurchasePlan,
    val lesson: Lesson? = null,
)

sealed interface PurchaseSuccessIntent {
    data class LessonChanged(val lesson: Lesson?) : PurchaseSuccessIntent
}
