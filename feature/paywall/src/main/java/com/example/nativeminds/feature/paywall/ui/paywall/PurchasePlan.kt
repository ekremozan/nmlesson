package com.example.nativeminds.feature.paywall.ui.paywall

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * The two mock pricing variants of the single premium entitlement — not different feature sets.
 *
 * [Serializable] so it can travel as a nav-route argument from the Paywall to the Purchase
 * Success screen without a separate string encoding at the call site. [Keep] because that same
 * route argument is only ever referenced reflectively by the navigation serializer, so a minified
 * build would otherwise be free to rename the constants out from under it.
 */
@Keep
@Serializable
enum class PurchasePlan {
    MONTHLY,
    YEARLY,
}
