package com.example.nativeminds.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.theme.NativeMindsTheme

/**
 * Multipreview that renders a composable in **both** themes.
 *
 * Every composable in the app carries a preview, and both themes are first-class — declaring the
 * light/dark pair by hand on each one is boilerplate that drifts. `uiMode` drives
 * `isSystemInDarkTheme()`, so [PreviewSurface] picks the right scheme with no parameter to pass.
 */
@Preview(name = "Light", group = "themes", showBackground = true)
@Preview(
    name = "Dark",
    group = "themes",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class ThemePreviews

/**
 * Preview host: applies the theme and paints the app background, so a preview shows the component
 * on the ground it actually sits on rather than on the tool's white canvas.
 */
@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    NativeMindsTheme {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
        ) {
            content()
        }
    }
}
