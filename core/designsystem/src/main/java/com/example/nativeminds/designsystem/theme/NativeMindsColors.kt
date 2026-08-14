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
    /**
     * Cover placeholder that sits in front of its neighbours — the middle card of the paywall's
     * hero stack. A step deeper than [cover] so the overlap reads as depth rather than as a seam.
     */
    val coverRaised: Color,
    /**
     * Ground of a full-bleed hero band, which is tinted a step away from the page so the band
     * itself is visible before any decoration is drawn on it.
     */
    val heroBand: Color,
    /** Opacity applied to the cover of a story the current user cannot open. */
    val lockedCoverAlpha: Float,
    /** Pill background of the "Premium" badge that sits on top of a locked cover. */
    val premiumBadgeBackground: Color,
    /** Lock glyph and label inside the premium badge. */
    val premiumBadgeContent: Color,
    /** Unselected bottom-navigation item. */
    val navInactive: Color,
    /**
     * Premium chip sitting on the page rather than on a cover.
     *
     * Separate from [premiumBadgeBackground]: that one has to hold its own against arbitrary cover
     * art, while this one sits on the paper ground and can be a flat tint.
     */
    val premiumChipBackground: Color,
    /** Lock glyph and label inside [premiumChipBackground]. */
    val premiumChipContent: Color,
    /** Filled portion of the reading-progress bar. */
    val readingProgress: Color,
    /** Round check beside a benefit line in the premium sheet. */
    val benefitCheckBackground: Color,
    /** The check glyph itself. */
    val benefitCheckContent: Color,
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
    coverRaised = Neutral400,
    heroBand = PaperSurface,
    lockedCoverAlpha = 0.55f,
    premiumBadgeBackground = PaperCard.copy(alpha = 0.94f),
    premiumBadgeContent = Accent700,
    navInactive = Ink.copy(alpha = 0.42f),
    premiumChipBackground = Accent200,
    premiumChipContent = Accent700,
    readingProgress = Sage,
    benefitCheckBackground = Sage200,
    benefitCheckContent = Sage700,
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
    coverRaised = InkCoverRaised,
    heroBand = InkSurface,
    lockedCoverAlpha = 0.55f,
    premiumBadgeBackground = InkGround.copy(alpha = 0.90f),
    premiumBadgeContent = AccentDark,
    navInactive = Parchment.copy(alpha = 0.38f),
    premiumChipBackground = Accent800,
    premiumChipContent = Accent300,
    readingProgress = Sage400,
    benefitCheckBackground = Sage800,
    benefitCheckContent = Sage300,
    isDark = true,
)

internal val LocalNativeMindsColors = staticCompositionLocalOf { LightNativeMindsColors }
