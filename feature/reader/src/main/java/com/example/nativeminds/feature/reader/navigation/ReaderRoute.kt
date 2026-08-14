package com.example.nativeminds.feature.reader.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.nativeminds.feature.reader.ui.ReaderScreen
import kotlinx.serialization.Serializable

/**
 * The story id is the destination's only argument, and it is typed rather than a string key.
 *
 * Reading it back through `SavedStateHandle` is also what makes the screen survive process death
 * without any restoration code of its own.
 */
@Serializable
data class ReaderRoute(val storyId: Long)

fun NavGraphBuilder.readerScreen(onBack: () -> Unit) {
    composable<ReaderRoute> {
        ReaderScreen(onBack = onBack)
    }
}
