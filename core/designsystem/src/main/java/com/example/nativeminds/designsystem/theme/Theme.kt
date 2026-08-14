package com.example.nativeminds.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Light scheme — warm paper ground.
 *
 * The design system has no red: warnings and errors are carried by the deep end of the terracotta
 * ramp, and success by sage. Mapping `error` onto terracotta instead of introducing a red keeps
 * error states inside the brand instead of making them look like a system dialog.
 */
private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Paper,
    primaryContainer = Accent100,
    onPrimaryContainer = Accent800,

    secondary = Sage600,
    onSecondary = Paper,
    secondaryContainer = Sage200,
    onSecondaryContainer = Sage800,

    tertiary = Accent700,
    onTertiary = Paper,
    tertiaryContainer = Accent200,
    onTertiaryContainer = Accent900,

    background = Paper,
    onBackground = Ink,

    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperSurface,
    onSurfaceVariant = Ink.copy(alpha = 0.70f),

    surfaceContainerLowest = PaperCard,
    surfaceContainerLow = Neutral100,
    surfaceContainer = PaperSurface,
    surfaceContainerHigh = Neutral200,
    surfaceContainerHighest = Neutral300,

    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = AccentDark,

    outline = Neutral500,
    outlineVariant = Ink.copy(alpha = 0.14f),

    error = Accent700,
    onError = Paper,
    errorContainer = Accent200,
    onErrorContainer = Accent900,

    scrim = Color(0xFF2E2B25),
)

/**
 * Dark scheme — same hierarchy on a warm ink ground.
 *
 * Roles are not simply inverted: the accent is lightened to [AccentDark] because the light-theme
 * terracotta goes muddy on ink, and container/on-container pairs swap ends of the ramp so
 * contrast stays in the same band as the light scheme.
 */
private val DarkColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = InkGround,
    primaryContainer = Accent800,
    onPrimaryContainer = Accent200,

    secondary = Sage400,
    onSecondary = InkGround,
    secondaryContainer = Sage800,
    onSecondaryContainer = Sage200,

    tertiary = Accent400,
    onTertiary = InkGround,
    tertiaryContainer = Accent900,
    onTertiaryContainer = Accent300,

    background = InkGround,
    onBackground = Parchment,

    surface = InkGround,
    onSurface = Parchment,
    surfaceVariant = InkSurface,
    onSurfaceVariant = Parchment.copy(alpha = 0.70f),

    surfaceContainerLowest = Color(0xFF171310),
    surfaceContainerLow = Color(0xFF201C19),
    surfaceContainer = InkSurface,
    surfaceContainerHigh = Color(0xFF332C28),
    surfaceContainerHighest = Color(0xFF453C33),

    inverseSurface = Parchment,
    inverseOnSurface = InkGround,
    inversePrimary = Accent,

    outline = Neutral500,
    outlineVariant = Parchment.copy(alpha = 0.16f),

    error = Accent400,
    onError = InkGround,
    errorContainer = Accent900,
    onErrorContainer = Accent200,

    scrim = Color(0xFF000000),
)

/**
 * The app theme.
 *
 * Dynamic color is deliberately **off**. The product's identity is the paper-and-terracotta
 * palette — the serif reading voice and the warm ground are what make it feel like a story app,
 * and letting the wallpaper repaint that would trade the brand for a personalization win the
 * content does not benefit from. (An app whose value is its own atmosphere is exactly the case
 * where Material advises against dynamic color.)
 */
@Composable
fun NativeMindsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val nativeMindsColors = if (darkTheme) DarkNativeMindsColors else LightNativeMindsColors

    CompositionLocalProvider(
        LocalNativeMindsColors provides nativeMindsColors,
        LocalNativeMindsTypography provides NativeMindsTextStyles,
        LocalNativeMindsSpacing provides NativeMindsSpacing(),
        LocalNativeMindsSizes provides NativeMindsSizes(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NativeMindsMaterialTypography,
            shapes = NativeMindsShapes,
            content = content,
        )
    }
}

/**
 * Accessor for the brand tokens, mirroring how `MaterialTheme.colorScheme` is reached.
 *
 * Use `MaterialTheme` for standard roles and this for the brand-specific ones:
 * ```
 * color = MaterialTheme.colorScheme.primary          // fills
 * color = NativeMindsTheme.colors.accentText         // accent text and icons
 * style = NativeMindsTheme.typography.storyTitle
 * ```
 */
object NativeMindsTheme {
    val colors: NativeMindsColors
        @Composable @ReadOnlyComposable get() = LocalNativeMindsColors.current

    val typography: NativeMindsTypography
        @Composable @ReadOnlyComposable get() = LocalNativeMindsTypography.current

    val spacing: NativeMindsSpacing
        @Composable @ReadOnlyComposable get() = LocalNativeMindsSpacing.current

    val sizes: NativeMindsSizes
        @Composable @ReadOnlyComposable get() = LocalNativeMindsSizes.current
}
