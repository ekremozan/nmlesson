package com.example.nativeminds.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.nativeminds.designsystem.R

/**
 * The design system uses three voices:
 *
 * - **Display** (Caprasimo) — the warm, chunky brand voice. Greetings, empty states, hero titles.
 * - **Reading** (Newsreader) — the editorial voice. Story titles, teasers, and body copy in the
 *   reader. A serif here is a product decision, not decoration: it is what makes a wall of text
 *   feel like a story rather than a settings screen.
 * - **UI** (Figtree) — the interface voice. Labels, chips, meta rows, navigation.
 *
 * The three families are declared once here, so a change of typeface is a change in one place.
 *
 * The fonts are **bundled** rather than fetched through the downloadable-fonts provider: the type
 * is part of the brand and the app has to look right on first launch and offline, which a provider
 * round-trip cannot guarantee. The cost is ~400 KB in the APK.
 *
 * Only the weights the design actually uses are shipped, as static instances rather than variable
 * fonts — variable-font axes need API 26 and `minSdk` here is 24, so static instances keep the
 * weights honest on the oldest supported devices instead of letting the platform synthesize them.
 * All three families are licensed under the SIL Open Font License (see THIRD_PARTY_LICENSES.md).
 */
val DisplayFontFamily = FontFamily(
    Font(R.font.caprasimo_regular, FontWeight.Normal),
)

val ReadingFontFamily = FontFamily(
    Font(R.font.newsreader_regular, FontWeight.Normal),
    Font(R.font.newsreader_medium, FontWeight.Medium),
)

val UiFontFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold),
)

/**
 * Material 3 type scale, carrying the *interface* voice.
 *
 * Material has a single body ramp, but this product has two body voices — UI copy and editorial
 * copy. The Material slots take the UI voice (matching the design system's `body` default), and
 * the editorial styles live in [NativeMindsTypography] where they are named for what they are.
 */
internal val NativeMindsMaterialTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 42.sp,
        lineHeight = 47.sp,
        letterSpacing = (-0.015).em,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.015).em,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 25.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.015).em,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 31.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 25.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 20.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.015).em,
    ),
    titleLarge = TextStyle(
        fontFamily = ReadingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ReadingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.5.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = UiFontFamily,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 17.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        lineHeight = 13.sp,
    ),
)

/**
 * Product-specific text styles, named for the role they play rather than for a size step.
 *
 * These exist because the Material scale cannot express "the story teaser" or "the premium badge"
 * — and naming them here is what stops those specs from being retyped, slightly differently, in
 * every screen that shows a story.
 */
@Immutable
data class NativeMindsTypography(
    /** "Good morning" above the user's name. */
    val greetingLabel: TextStyle,
    /** The user's name — the brand's warmest moment on the home screen. */
    val greetingName: TextStyle,
    /** Text inside the search field. */
    val searchInput: TextStyle,
    /** Category filter chips. */
    val chip: TextStyle,
    /** "FOR YOU" / "RESULTS" — uppercase applied at the call site, not baked into the string. */
    val sectionHeading: TextStyle,
    /** "6 stories" opposite the section heading. */
    val sectionCount: TextStyle,
    /** Category kicker on a story card. */
    val storyCategory: TextStyle,
    /** Story title on a card — editorial voice, clamps to two lines. */
    val storyTitle: TextStyle,
    /** One-line teaser under the title. */
    val storyTeaser: TextStyle,
    /** "6 min" and the audio affordance next to it. */
    val storyMeta: TextStyle,
    /** The "PREMIUM" pill on a locked cover. */
    val premiumBadge: TextStyle,
    /** Bottom navigation labels. */
    val navLabel: TextStyle,
    /** Empty-state headline. */
    val emptyTitle: TextStyle,
    /** Empty-state explanation. */
    val emptyBody: TextStyle,
    /** Long-form story body in the reader. */
    val readingBody: TextStyle,
)

internal val NativeMindsTextStyles = NativeMindsTypography(
    greetingLabel = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.02.em,
    ),
    greetingName = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 31.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).em,
    ),
    searchInput = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
    chip = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
    ),
    sectionHeading = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.13.em,
    ),
    sectionCount = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
    ),
    storyCategory = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.06.em,
    ),
    storyTitle = TextStyle(
        fontFamily = ReadingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.5.sp,
        lineHeight = 21.7.sp,
        letterSpacing = (-0.005).em,
    ),
    storyTeaser = TextStyle(
        fontFamily = ReadingFontFamily,
        fontSize = 13.5.sp,
        lineHeight = 18.2.sp,
    ),
    storyMeta = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.5.sp,
    ),
    premiumBadge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.5.sp,
        letterSpacing = 0.08.em,
    ),
    navLabel = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
    ),
    emptyTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 25.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.02).em,
    ),
    emptyBody = TextStyle(
        fontFamily = ReadingFontFamily,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    readingBody = TextStyle(
        fontFamily = ReadingFontFamily,
        fontSize = 17.sp,
        lineHeight = 28.sp,
    ),
)

internal val LocalNativeMindsTypography = staticCompositionLocalOf { NativeMindsTextStyles }
