package com.example.nativeminds.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The design system's spacing scale, rounded to whole dp.
 *
 * The steps are irregular (4 / 9 / 13 / 18 / 26 / 35) because the scale is geometric rather than
 * a multiple of 8 — this is what produces the slightly loose, editorial rhythm of the screens.
 * Rounding them here keeps every screen on the same grid instead of scattering one-off values.
 */
@Immutable
data class NativeMindsSpacing(
    /** Hairline gaps: icon-to-label inside a badge. */
    val xs: Dp = 4.dp,
    /** Gaps between peers in a row: chips, meta items. */
    val sm: Dp = 9.dp,
    /** Padding inside a card. */
    val md: Dp = 13.dp,
    /** Gaps between stacked blocks inside a section. */
    val lg: Dp = 18.dp,
    /** Screen horizontal padding — the app's main margin. */
    val screen: Dp = 26.dp,
    /** Separation between major sections. */
    val xl: Dp = 35.dp,
)

internal val LocalNativeMindsSpacing = staticCompositionLocalOf { NativeMindsSpacing() }
