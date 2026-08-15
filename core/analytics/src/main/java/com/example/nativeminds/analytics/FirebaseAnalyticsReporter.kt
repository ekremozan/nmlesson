package com.example.nativeminds.analytics

import android.os.Bundle
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.observability.ErrorReporter
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single point every [AnalyticsEvent] routes through to a real backend, mirroring
 * [com.example.nativeminds.domain.observability.ErrorReporter]'s shape: one call in, never a
 * throw out — a failure here reaches [errorReporter] instead of the caller.
 */
@Singleton
class FirebaseAnalyticsReporter @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val errorReporter: ErrorReporter,
) : AnalyticsReporter {
    override fun log(event: AnalyticsEvent) {
        runCatching {
            val (name, params) = event.toFirebaseEvent()
            firebaseAnalytics.logEvent(name, params)
        }.onFailure { throwable ->
            errorReporter.report(throwable, "analytics:${event::class.simpleName}")
        }
    }
}

private fun AnalyticsEvent.toFirebaseEvent(): Pair<String, Bundle> = when (this) {
    is AnalyticsEvent.ScreenViewed -> "screen_view" to Bundle().apply {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        previousScreenName?.let { putString("previous_screen_name", it) }
        putString("source", source.name)
    }

    is AnalyticsEvent.LessonSelected -> "lesson_selected" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("lesson_title", lessonTitle)
        putInt("list_index", listIndex)
    }

    is AnalyticsEvent.ContentViewed -> "content_viewed" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("access_level", accessLevel.name)
    }

    is AnalyticsEvent.ListenStarted -> "listen_started" to Bundle().apply {
        putLong("lesson_id", lessonId)
    }

    is AnalyticsEvent.ListenStopped -> "listen_stopped" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("reason", reason.name)
        putInt("progress_percent", progressPercent)
    }

    is AnalyticsEvent.PaywallShown -> "paywall_shown" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("trigger_source", triggerSource)
    }

    is AnalyticsEvent.PaywallPurchaseClicked -> "paywall_purchase_clicked" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("plan", plan)
    }

    is AnalyticsEvent.SubscriptionStarted -> "subscription_started" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("plan", plan)
    }

    is AnalyticsEvent.PurchaseDeclined -> "purchase_declined" to Bundle().apply {
        putLong("lesson_id", lessonId)
        putString("reason", reason)
    }

    is AnalyticsEvent.PaywallDismissed -> "paywall_dismissed" to Bundle().apply {
        putLong("lesson_id", lessonId)
    }

    is AnalyticsEvent.RestorePurchasesClicked -> "restore_purchases_clicked" to Bundle().apply {
        putLong("lesson_id", lessonId)
    }

    is AnalyticsEvent.LessonsFiltered -> "lessons_filtered" to Bundle().apply {
        query?.let { putString("query", it) }
        subject?.let { putString("subject", it) }
    }

    is AnalyticsEvent.AiFeatureUsed -> "ai_feature_used" to Bundle().apply {
        lessonId?.let { putLong("lesson_id", it) }
        putString("feature_name", featureName)
    }
}
