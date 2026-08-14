package com.example.nativeminds.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw palette of the "Organic" design system.
 *
 * These are the only literal colors in the app — everything else references them through
 * [NativeMindsColors] or the Material 3 color scheme in `Theme.kt`. The ramps are generated in
 * OKLCH on one shared lightness scale, so the same step of any role matches the others in visual
 * value (e.g. [Accent400] and [Sage400] read as equally light).
 *
 * Do not use these directly in composables — use `NativeMindsTheme.colors` or
 * `MaterialTheme.colorScheme` so light/dark switching keeps working.
 */

/** Page background, light theme. */
internal val Paper = Color(0xFFF5EAD8)

/** Raised surface on paper: search field, avatar, empty-state medallion. */
internal val PaperSurface = Color(0xFFEBDDC5)

/** Card ground on paper — lighter than the page, the way a card catches light. */
internal val PaperCard = Color(0xFFFDF7EC)

/** Primary text/ink, light theme. */
internal val Ink = Color(0xFF201E1D)

/** Page background, dark theme — warm ink, not neutral black. */
internal val InkGround = Color(0xFF1C1815)

/** Raised surface and card ground on the ink ground. */
internal val InkSurface = Color(0xFF272220)

/** Cover placeholder that sits in front of its neighbours, dark theme. */
internal val InkCoverRaised = Color(0xFF554A3E)

/** Primary text on the ink ground. */
internal val Parchment = Color(0xFFF3E9D9)

internal val Neutral100 = Color(0xFFF9F4ED)
internal val Neutral200 = Color(0xFFEEE7DB)
internal val Neutral300 = Color(0xFFDCD3C4)
internal val Neutral400 = Color(0xFFC0B6A5)
internal val Neutral500 = Color(0xFFA19786)
internal val Neutral600 = Color(0xFF82796A)
internal val Neutral700 = Color(0xFF645C50)
internal val Neutral800 = Color(0xFF474238)
internal val Neutral900 = Color(0xFF2E2B25)

internal val Accent100 = Color(0xFFFFF2EB)
internal val Accent200 = Color(0xFFFFE1D0)
internal val Accent300 = Color(0xFFFFC6A5)
internal val Accent400 = Color(0xFFF6A06B)
internal val Accent500 = Color(0xFFD67F48)
internal val Accent600 = Color(0xFFB2622D)
internal val Accent700 = Color(0xFF8C491A)
internal val Accent800 = Color(0xFF643312)
internal val Accent900 = Color(0xFF402310)

/** The brand accent used for fills (selected chips, primary buttons). */
internal val Accent = Color(0xFFC67139)

/** Accent tuned for the dark ground — the light-theme accent is too dim on ink. */
internal val AccentDark = Color(0xFFE0954F)

internal val Sage100 = Color(0xFFF0FAE1)
internal val Sage200 = Color(0xFFE1EECC)
internal val Sage300 = Color(0xFFCCDBB2)
internal val Sage400 = Color(0xFFAEBF92)
internal val Sage500 = Color(0xFF8FA073)
internal val Sage600 = Color(0xFF728157)
internal val Sage700 = Color(0xFF56633F)
internal val Sage800 = Color(0xFF3D472B)
internal val Sage900 = Color(0xFF272E1B)

/** The secondary accent: category labels, audio affordances, "available offline" states. */
internal val Sage = Color(0xFF7A8A5E)
