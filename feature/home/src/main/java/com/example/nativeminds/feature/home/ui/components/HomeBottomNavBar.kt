package com.example.nativeminds.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.R

/**
 * Bottom navigation for Home/Library/Ask AI. Visual only for this pass — Home is the only
 * destination that exists yet, so the other two items are not wired to navigation.
 */
@Composable
fun HomeBottomNavBar(modifier: Modifier = Modifier) {
    val background = MaterialTheme.colorScheme.background
    // Solid for the bottom ~62% of the bar, fading to transparent only at the top edge — matches
    // the design's `linear-gradient(to top, background 62%, transparent)`, not a bar that's
    // transparent all the way through.
    val scrim = Brush.verticalGradient(
        0f to background.copy(alpha = 0f),
        0.38f to background,
        1f to background,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(scrim)
            .padding(horizontal = 34.dp)
            .padding(bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        NavItem(
            label = stringResource(R.string.home_nav_home),
            tint = MaterialTheme.colorScheme.primary,
            icon = { color -> NativeMindsIcons.Home(tint = color) },
        )
        NavItem(
            label = stringResource(R.string.home_nav_library),
            tint = NativeMindsTheme.colors.navInactive,
            icon = { color -> NativeMindsIcons.Library(tint = color) },
        )
        NavItem(
            label = stringResource(R.string.home_nav_ask_ai),
            tint = NativeMindsTheme.colors.navInactive,
            icon = { color -> NativeMindsIcons.Sparkle(tint = color) },
        )
    }
}

@Composable
private fun NavItem(
    label: String,
    tint: Color,
    icon: @Composable (Color) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon(tint)
        Text(text = label, style = NativeMindsTheme.typography.navLabel, color = tint)
    }
}

@ThemePreviews
@Composable
private fun HomeBottomNavBarPreview() {
    PreviewSurface(padding = 0.dp) {
        HomeBottomNavBar()
    }
}

/** The item alone, in both states — active (primary) and inactive (muted). */
@ThemePreviews
@Composable
private fun NavItemPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            NavItem(
                label = stringResource(R.string.home_nav_home),
                tint = MaterialTheme.colorScheme.primary,
                icon = { color -> NativeMindsIcons.Home(tint = color) },
            )
            NavItem(
                label = stringResource(R.string.home_nav_library),
                tint = NativeMindsTheme.colors.navInactive,
                icon = { color -> NativeMindsIcons.Library(tint = color) },
            )
        }
    }
}
