package com.example.nativeminds.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Living specimen of the design system: every token rendered, in both themes.
 *
 * This is the fastest way to catch a palette regression — a role that went unreadable in dark, or
 * a text style that drifted — without running the app.
 */
@Composable
private fun ThemeSpecimen() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(NativeMindsTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.lg),
    ) {
        SpecimenSection("Material roles") {
            SwatchRow("primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            SwatchRow("secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
            SwatchRow("tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
            SwatchRow("error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
            SwatchRow("primaryContainer", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            SwatchRow("secondaryContainer", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        }

        SpecimenSection("Surfaces") {
            SwatchRow("background", MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onBackground)
            SwatchRow("surfaceContainerLowest", MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.colorScheme.onSurface)
            SwatchRow("surfaceContainer", MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurface)
            SwatchRow("surfaceContainerHighest", MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface)
        }

        SpecimenSection("Brand roles") {
            SwatchRow("accentText", NativeMindsTheme.colors.accentText, MaterialTheme.colorScheme.background)
            SwatchRow("cover", NativeMindsTheme.colors.cover, MaterialTheme.colorScheme.onSurface)
            SwatchRow("premiumBadge", NativeMindsTheme.colors.premiumBadgeBackground, NativeMindsTheme.colors.premiumBadgeContent)
            SwatchRow("navInactive", NativeMindsTheme.colors.navInactive, MaterialTheme.colorScheme.background)
        }

        SpecimenSection("Type") {
            Specimen("Good morning", NativeMindsTheme.typography.greetingLabel, NativeMindsTheme.colors.textMuted)
            Specimen("Ozan", NativeMindsTheme.typography.greetingName)
            Specimen("FOR YOU", NativeMindsTheme.typography.sectionHeading, NativeMindsTheme.colors.textMuted)
            Specimen("FICTION", NativeMindsTheme.typography.storyCategory, MaterialTheme.colorScheme.secondary)
            Specimen("The Lighthouse Keeper's Last Letter", NativeMindsTheme.typography.storyTitle)
            Specimen(
                "Forty years of weather notes, and one page he never sent.",
                NativeMindsTheme.typography.storyTeaser,
                NativeMindsTheme.colors.textMuted,
            )
            Specimen("6 min", NativeMindsTheme.typography.storyMeta, NativeMindsTheme.colors.textMuted)
            Specimen("PREMIUM", NativeMindsTheme.typography.premiumBadge, NativeMindsTheme.colors.premiumBadgeContent)
            Specimen("Nothing here yet", NativeMindsTheme.typography.emptyTitle)
        }
    }
}

@Composable
private fun SpecimenSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.xs)) {
        Text(
            text = title.uppercase(),
            style = NativeMindsTheme.typography.sectionHeading,
            color = NativeMindsTheme.colors.textMuted,
            modifier = Modifier.padding(bottom = NativeMindsTheme.spacing.xs),
        )
        content()
    }
}

@Composable
private fun SwatchRow(name: String, background: Color, foreground: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(background, RoundedCornerShape(12.dp))
                .border(1.dp, NativeMindsTheme.colors.cardBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("Aa", style = NativeMindsTheme.typography.storyMeta, color = foreground)
        }
        Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun Specimen(
    text: String,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Text(text = text, style = style, color = color)
}

@Preview(name = "Theme — light", showBackground = true, heightDp = 1400)
@Composable
private fun ThemeSpecimenLightPreview() {
    NativeMindsTheme(darkTheme = false) { ThemeSpecimen() }
}

@Preview(
    name = "Theme — dark",
    showBackground = true,
    heightDp = 1400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ThemeSpecimenDarkPreview() {
    NativeMindsTheme(darkTheme = true) { ThemeSpecimen() }
}
