package com.example.nativeminds.domain

import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter

class RecordingAnalyticsReporter : AnalyticsReporter {
    val logged = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        logged += event
    }
}
