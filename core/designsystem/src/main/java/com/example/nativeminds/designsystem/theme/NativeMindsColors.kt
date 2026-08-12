package com.example.nativeminds.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand color roles that Material 3 has no slot for.
 *
 * Everything that maps cleanly onto a Material role (primary, secondary, surface containers,
 * outline, …) lives in the color schemes in `Theme.kt` instead — this class only carries what
 * would otherwise be hardcoded in composables.
 *
 * Reach for these via `NativeMindsTheme.colors`.
 */
@Immutable
data class NativeMindsColors(
    /**
     * Accent tuned for text and icons sitting directly on the page.
     *
     * Deliberately darker than `colorScheme.primary`: the fill accent (#C67139) only reaches
     * ~2.7:1 against the paper ground, which fails WCAG AA for text. Fills use `primary`,
     * anything with a glyph uses this.
     */
    val accentText: Color,
    /** Hairline edge on cards — carries the card shape where the fill contrast is nearly nil. */
    val cardBorder: Color,
    /** Secondary text: timestamps, section headings, teaser copy. */
    val textMuted: Color,
    /** Tertiary text: counts and other text that should recede below [textMuted]. */
    val textSubtle: Color,
    /** Ground for cover art placeholders and other image slots before they load. */
    val cover: Color,
    /** Opacity applied to the cover of a story the current user cannot open. */
    val lockedCoverAlpha: Float,
    /** Pill background of the "Premium" badge that sits on top of a locked cover. */
    val premiumBadgeBackground: Color,
    /** Lock glyph and label inside the premium badge. */
    val premiumBadgeContent: Color,
    /** Unselected bottom-navigation item. */
    val navInactive: Color,
    /** True when this palette is the dark one — for the few places that must branch on it. */
    val isDark: Boolean,
)

/**
 * Light palette — warm paper ground.
 */
internal val LightNativeMindsColors = NativeMindsColors(
    accentText = Accent600,
    cardBorder = Ink.copy(alpha = 0.07f),
    textMuted = Ink.copy(alpha = 0.55f),
    textSubtle = Ink.copy(alpha = 0.45f),
    cover = Neutral300,
    lockedCoverAlpha = 0.55f,
    premiumBadgeBackground = PaperCard.copy(alpha = 0.94f),
    premiumBadgeContent = Accent700,
    navInactive = Ink.copy(alpha = 0.42f),
    isDark = false,
)

/**
 * Dark palette — same hierarchy on a warm ink ground.
 *
 * Note the alphas are lower than the light palette's: light text on a dark ground reads heavier at
 * equal opacity, so muted text needs to be pushed further back to keep the same visual weight.
 */
internal val DarkNativeMindsColors = NativeMindsColors(
    accentText = AccentDark,
    cardBorder = Parchment.copy(alpha = 0.07f),
    textMuted = Parchment.copy(alpha = 0.50f),
    textSubtle = Parchment.copy(alpha = 0.40f),
    cover = Color(0xFF453C33),
    lockedCoverAlpha = 0.55f,
    premiumBadgeBackground = InkGround.copy(alpha = 0.90f),
    premiumBadgeContent = AccentDark,
    navInactive = Parchment.copy(alpha = 0.38f),
    isDark = true,
)

internal val LocalNativeMindsColors = staticCompositionLocalOf { LightNativeMindsColors }
