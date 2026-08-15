package com.example.nativeminds.domain.observability

import com.example.nativeminds.domain.RecordingAnalyticsReporter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `AiFeatureUsed` has no call site yet — the AI feature itself is future work (see
 * specs/006-firebase-analytics/research.md). This proves the event and reporter contract already
 * work end-to-end so wiring in a real call site later is a one-line change.
 */
class AnalyticsEventTest {
    @Test
    fun aiFeatureUsedRoundTripsThroughAnalyticsReporter() {
        val reporter = RecordingAnalyticsReporter()
        val event = AnalyticsEvent.AiFeatureUsed(lessonId = 1L, featureName = "story_summary")

        reporter.log(event)

        assertEquals(listOf(event), reporter.logged)
    }
}
