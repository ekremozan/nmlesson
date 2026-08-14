package com.example.nativeminds.feature.paywall.navigation

import kotlinx.serialization.Serializable

/**
 * [lessonId]/[progressPercent] identify the lesson and reading position that triggered the gate, so
 * the Purchase Success screen can offer to resume exactly where the reader left off.
 */
@Serializable
data class PaywallRoute(val lessonId: Long, val progressPercent: Int)
