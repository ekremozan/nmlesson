package com.example.nativeminds.feature.paywall.navigation

import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import kotlinx.serialization.Serializable

/** Carries the same lesson reference the [PaywallRoute] opened with, plus which plan was bought. */
@Serializable
data class PurchaseSuccessRoute(
    val lessonId: Long,
    val progressPercent: Int,
    val plan: PurchasePlan,
)
