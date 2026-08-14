package com.example.nativeminds.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fixed component dimensions the design specifies by number rather than by scale step.
 *
 * These are deliberately separate from [NativeMindsSpacing]: spacing is a rhythm applied between
 * things, while these are the measured height of a particular control. Mixing them would make the
 * spacing scale meaningless the first time a 330 dp fade was added to it.
 *
 * Reach for these via `NativeMindsTheme.sizes`.
 */
@Immutable
data class NativeMindsSizes(
    /** Round, borderless icon button — back and overflow in the reader's top bar. */
    val iconButton: Dp = 40.dp,
    /** Height of the reader's top bar, which is shorter than a Material app bar. */
    val readerTopBar: Dp = 52.dp,
    /** Cover art placeholder above a lesson's first paragraph. */
    val readerCover: Dp = 150.dp,
    /** Inset of the hairline rule that closes a lesson, measured from each side. */
    val readerClosingRuleInset: Dp = 60.dp,
    /** How far the narration highlight is grown past the word it marks, on every side. */
    val narrationHighlightPadding: Dp = 3.dp,
    /** Corner radius of that highlight, measured on its grown outer edge. */
    val narrationHighlightRadius: Dp = 8.dp,
    /**
     * Height of the gradient that dissolves a restricted lesson into the page.
     *
     * Tall on purpose: the text has to become unreadable well before it stops being drawn, or the
     * withheld part reads as a rendering bug rather than a boundary.
     */
    val bodyFade: Dp = 330.dp,
    /** The floating listen/progress pill at the foot of the reader. */
    val listenPill: Dp = 60.dp,
    /**
     * Gradient behind that pill.
     *
     * Without it the last line of a paragraph slides under the pill and stays half-readable, which
     * looks like a bug rather than like a control floating over a page.
     */
    val footerScrim: Dp = 120.dp,
    /** The terracotta action inside that pill. */
    val listenPillAction: Dp = 44.dp,
    /** Track of the reading-progress bar. */
    val progressTrack: Dp = 5.dp,
    /** Primary call-to-action button. */
    val actionButton: Dp = 52.dp,
    /** Round icon badge in front of a paywall/success benefit line. */
    val benefitIcon: Dp = 42.dp,
    /**
     * Full-bleed hero band at the head of the paywall.
     *
     * Measured from the very top of the screen, status bar included — the band is meant to sit
     * behind it, so this is not a below-the-inset height.
     */
    val paywallHero: Dp = 290.dp,
    /** Gradient that dissolves the bottom of [paywallHero] into the page ground. */
    val paywallHeroFade: Dp = 120.dp,
)

internal val LocalNativeMindsSizes = staticCompositionLocalOf { NativeMindsSizes() }
