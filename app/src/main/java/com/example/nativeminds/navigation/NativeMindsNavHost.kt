package com.example.nativeminds.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.nativeminds.feature.home.navigation.HomeRoute
import com.example.nativeminds.feature.home.navigation.homeScreen
import com.example.nativeminds.feature.paywall.navigation.PaywallRoute
import com.example.nativeminds.feature.paywall.navigation.PurchaseSuccessRoute
import com.example.nativeminds.feature.paywall.navigation.paywallScreen
import com.example.nativeminds.feature.paywall.navigation.purchaseSuccessScreen
import com.example.nativeminds.feature.reader.navigation.ReaderRoute
import com.example.nativeminds.feature.reader.navigation.readerScreen
import com.example.nativeminds.feature.settings.navigation.SettingsRoute
import com.example.nativeminds.feature.settings.navigation.settingsScreen

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

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen(
            onLessonClick = { lessonId -> navController.navigate(ReaderRoute(lessonId)) },
            onProfileClick = { navController.navigate(SettingsRoute) },
        )
        readerScreen(
            onBack = navController::navigateUp,
            onUnlockRequested = { lessonId, progressPercent ->
                navController.navigate(PaywallRoute(lessonId, progressPercent))
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
            onPremiumClick = { navController.navigate(PaywallRoute(lessonId = -1L, progressPercent = 0)) },
        )
    }
}
