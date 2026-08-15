package com.example.nativeminds.navigation

import androidx.lifecycle.ViewModel
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.observability.NavigationSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * The only way [NativeMindsNavHost] reaches [AnalyticsReporter]: the nav host composable is not
 * itself Hilt-injectable, so this small scoped ViewModel is what carries the dependency in
 * without falling back to a service locator.
 */
@HiltViewModel
class NavigationAnalyticsViewModel @Inject constructor(
    private val analyticsReporter: AnalyticsReporter,
) : ViewModel() {
    fun trackScreenView(screenName: String, previousScreenName: String?, source: NavigationSource) {
        analyticsReporter.log(
            AnalyticsEvent.ScreenViewed(
                screenName = screenName,
                previousScreenName = previousScreenName,
                source = source,
            ),
        )
    }

    /** For events raised at a plain navigation callback rather than a screen transition. */
    fun log(event: AnalyticsEvent) {
        analyticsReporter.log(event)
    }
}
