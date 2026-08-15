package com.example.nativeminds.domain.observability

/**
 * Where every user-behavior event this app cares about goes, so no feature module ever calls an
 * analytics SDK directly.
 *
 * Implementations must never throw.
 */
interface AnalyticsReporter {
    fun log(event: AnalyticsEvent)
}
