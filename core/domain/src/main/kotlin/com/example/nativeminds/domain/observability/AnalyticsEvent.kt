package com.example.nativeminds.domain.observability

/**
 * Every user action this app reports to analytics, closed so a new kind of event is a compile
 * error everywhere it isn't handled yet rather than a silently unmapped string.
 */
sealed interface AnalyticsEvent {
    data class ScreenViewed(
        val screenName: String,
        val previousScreenName: String?,
        val source: NavigationSource,
    ) : AnalyticsEvent

    data class LessonSelected(
        val lessonId: Long,
        val lessonTitle: String,
        val listIndex: Int,
    ) : AnalyticsEvent

    data class ContentViewed(
        val lessonId: Long,
        val accessLevel: AccessLevel,
    ) : AnalyticsEvent

    data class ListenStarted(val lessonId: Long) : AnalyticsEvent

    data class ListenStopped(
        val lessonId: Long,
        val reason: ListenStopReason,
        val progressPercent: Int,
    ) : AnalyticsEvent

    data class PaywallShown(
        val lessonId: Long,
        val triggerSource: String,
    ) : AnalyticsEvent

    data class PaywallPurchaseClicked(
        val lessonId: Long,
        val plan: String,
    ) : AnalyticsEvent

    data class SubscriptionStarted(
        val lessonId: Long,
        val plan: String,
    ) : AnalyticsEvent

    data class PurchaseDeclined(
        val lessonId: Long,
        val reason: String,
    ) : AnalyticsEvent

    data class PaywallDismissed(val lessonId: Long) : AnalyticsEvent

    data class RestorePurchasesClicked(val lessonId: Long) : AnalyticsEvent

    data class LessonsFiltered(
        val query: String?,
        val subject: String?,
    ) : AnalyticsEvent

    data class AiFeatureUsed(
        val lessonId: Long?,
        val featureName: String,
    ) : AnalyticsEvent
}

enum class NavigationSource { FORWARD, BACK }

enum class AccessLevel { FULL, PREVIEW }

enum class ListenStopReason { PAUSED, COMPLETED, SCREEN_LEFT }
