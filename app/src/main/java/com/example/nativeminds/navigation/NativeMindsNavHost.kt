package com.example.nativeminds.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.NavigationSource
import com.example.nativeminds.feature.home.navigation.HomeRoute
import com.example.nativeminds.feature.home.navigation.homeScreen
import com.example.nativeminds.feature.paywall.navigation.PaywallRoute
import com.example.nativeminds.feature.paywall.navigation.PurchaseSuccessRoute
import com.example.nativeminds.feature.paywall.navigation.paywallScreen
import com.example.nativeminds.feature.paywall.navigation.purchaseSuccessScreen
import com.example.nativeminds.feature.quiz.navigation.QuizRoute
import com.example.nativeminds.feature.quiz.navigation.quizScreen
import com.example.nativeminds.feature.reader.navigation.ReaderRoute
import com.example.nativeminds.feature.reader.navigation.readerScreen
import com.example.nativeminds.feature.settings.navigation.SettingsRoute
import com.example.nativeminds.feature.settings.navigation.settingsScreen

/** The stable name each destination reports to analytics, independent of its route arguments. */
private fun screenNameOf(destination: NavDestination): String? = when {
    destination.hasRoute<HomeRoute>() -> "home"
    destination.hasRoute<ReaderRoute>() -> "reader"
    destination.hasRoute<QuizRoute>() -> "quiz"
    destination.hasRoute<PaywallRoute>() -> "paywall"
    destination.hasRoute<PurchaseSuccessRoute>() -> "purchase_success"
    destination.hasRoute<SettingsRoute>() -> "settings"
    else -> null
}

/**
 * The only place that knows every destination exists.
 *
 * Each feature declares its own route and entry point, so adding a screen never means editing
 * another feature — and no feature module depends on another. Home stays on the back stack while
 * the reader is open, which is what keeps its query, filter and scroll position intact for free
 * when the reader is dismissed.
 */
@Composable
fun NativeMindsNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navigationAnalyticsViewModel: NavigationAnalyticsViewModel = hiltViewModel()
    val screenStack = remember { mutableListOf<String>() }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val screenName = screenNameOf(destination) ?: return@OnDestinationChangedListener
            val previousIndex = screenStack.size - 2
            val isBack = previousIndex >= 0 && screenStack[previousIndex] == screenName
            val previousScreenName = screenStack.lastOrNull()
            if (isBack) {
                screenStack.removeAt(screenStack.size - 1)
            } else {
                screenStack.add(screenName)
            }
            navigationAnalyticsViewModel.trackScreenView(
                screenName = screenName,
                previousScreenName = previousScreenName,
                source = if (isBack) NavigationSource.BACK else NavigationSource.FORWARD,
            )
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen(
            onLessonClick = { lessonId, title, index ->
                navigationAnalyticsViewModel.log(AnalyticsEvent.LessonSelected(lessonId, title, index))
                navController.navigate(ReaderRoute(lessonId))
            },
            onProfileClick = { navController.navigate(SettingsRoute) },
        )
        readerScreen(
            onBack = navController::navigateUp,
            onUnlockRequested = { lessonId, progressPercent ->
                navController.navigate(PaywallRoute(lessonId, progressPercent, triggerSource = "reader_unlock"))
            },
            onTestRequested = { lessonId -> navController.navigate(QuizRoute(lessonId)) },
        )
        quizScreen(
            onBack = navController::navigateUp,
            onPaywallRequested = { lessonId ->
                navController.navigate(PaywallRoute(lessonId, progressPercent = 0, triggerSource = "quiz_locked"))
            },
        )
        paywallScreen(
            onClose = navController::navigateUp,
            onPurchased = { lessonId, progressPercent, plan ->
                navController.popBackStack()
                navController.navigate(PurchaseSuccessRoute(lessonId, progressPercent, plan))
            },
        )
        purchaseSuccessScreen(
            onContinueReading = { navController.popBackStack() },
            onExploreLibrary = { navController.popBackStack(HomeRoute, inclusive = false) },
        )
        settingsScreen(
            onBack = navController::navigateUp,
            onPremiumClick = {
                navController.navigate(
                    PaywallRoute(lessonId = -1L, progressPercent = 0, triggerSource = "settings_premium"),
                )
            },
        )
    }
}
