package com.example.nativeminds.navigation

import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.NavigationSource
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationAnalyticsViewModelTest {
    private val analyticsReporter = RecordingAnalyticsReporter()
    private val viewModel = NavigationAnalyticsViewModel(analyticsReporter)

    @Test
    fun `trackScreenView logs a ScreenViewed event with the given fields`() {
        viewModel.trackScreenView(
            screenName = "reader",
            previousScreenName = "home",
            source = NavigationSource.FORWARD,
        )

        assertEquals(
            listOf(
                AnalyticsEvent.ScreenViewed(
                    screenName = "reader",
                    previousScreenName = "home",
                    source = NavigationSource.FORWARD,
                ),
            ),
            analyticsReporter.logged,
        )
    }

    @Test
    fun `trackScreenView allows a null previous screen`() {
        viewModel.trackScreenView(
            screenName = "home",
            previousScreenName = null,
            source = NavigationSource.BACK,
        )

        val event = analyticsReporter.logged.single() as AnalyticsEvent.ScreenViewed
        assertEquals(null, event.previousScreenName)
        assertEquals(NavigationSource.BACK, event.source)
    }
}
