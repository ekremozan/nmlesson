package com.example.nativeminds.domain.observability

import com.example.nativeminds.domain.RecordingAnalyticsReporter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `AiFeatureUsed` is now logged by `QuizViewModel` (see specs/007-ai-quiz-generation). This proves
 * the event and reporter contract work end-to-end independent of any one feature's call site.
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
