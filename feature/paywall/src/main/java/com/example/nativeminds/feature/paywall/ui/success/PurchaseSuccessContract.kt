package com.example.nativeminds.feature.paywall.ui.success

import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.model.Story

/**
 * [story] starts `null` while [com.example.nativeminds.domain.repository.StoryRepository.story]
 * has not emitted yet — the same "not stored yet" convention the reader already uses, not a
 * distinct loading flag to keep in step.
 */
data class PurchaseSuccessUiState(
    val storyId: Long,
    val progressPercent: Int,
    val plan: PurchasePlan,
    val story: Story? = null,
)

sealed interface PurchaseSuccessIntent {
    data class StoryChanged(val story: Story?) : PurchaseSuccessIntent
}
