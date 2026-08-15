package com.example.nativeminds.feature.paywall.navigation

import kotlinx.serialization.Serializable

/**
 * [lessonId]/[progressPercent] identify the lesson and reading position that triggered the gate, so
 * the Purchase Success screen can offer to resume exactly where the reader left off. [triggerSource]
 * identifies which screen opened the paywall, for the `paywall_shown` analytics event.
 */
@Serializable
data class PaywallRoute(val lessonId: Long, val progressPercent: Int, val triggerSource: String)
