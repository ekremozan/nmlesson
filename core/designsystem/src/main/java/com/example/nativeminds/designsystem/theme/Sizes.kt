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
    /** Cover art placeholder above a story's first paragraph. */
    val readerCover: Dp = 150.dp,
    /** Inset of the hairline rule that closes a story, measured from each side. */
    val readerClosingRuleInset: Dp = 60.dp,
    /**
     * Height of the gradient that dissolves a restricted story into the page.
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
    /** Circular lock medallion that overhangs the top edge of the unlock sheet. */
    val unlockMedallion: Dp = 48.dp,
    /** Round check beside each benefit line in the unlock sheet. */
    val benefitCheck: Dp = 22.dp,
    /** Primary call-to-action button. */
    val actionButton: Dp = 52.dp,
)

internal val LocalNativeMindsSizes = staticCompositionLocalOf { NativeMindsSizes() }
