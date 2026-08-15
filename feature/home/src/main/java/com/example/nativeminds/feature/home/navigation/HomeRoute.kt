package com.example.nativeminds.feature.home.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.nativeminds.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/**
 * The feature declares its own destination, so the app module composes the graph without knowing
 * anything about the screen behind it.
 *
 * [onLessonClick] and [onProfileClick] are plain callbacks rather than `HomeIntent`s: opening the
 * reader or settings changes no home state, so routing them through the reducer would mean an
 * identity reduction and a branch in the ViewModel — the one shape the MVI rules exist to forbid.
 */
fun NavGraphBuilder.homeScreen(
    onLessonClick: (lessonId: Long, title: String, index: Int) -> Unit,
    onProfileClick: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(
            onLessonClick = onLessonClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        )
    }
}
