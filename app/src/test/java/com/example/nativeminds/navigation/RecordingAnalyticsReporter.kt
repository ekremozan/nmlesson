package com.example.nativeminds.navigation

import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter

/**
 * Local copy of `core/domain`'s test-only fake: Gradle doesn't expose another module's `test`
 * source set without a `java-test-fixtures` setup this project doesn't have, so each module that
 * needs it keeps its own tiny copy rather than adding a new build convention for one class.
 */
class RecordingAnalyticsReporter : AnalyticsReporter {
    val logged = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        logged += event
    }
}
