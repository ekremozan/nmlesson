package com.example.nativeminds.feature.paywall.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.theme.NativeMindsTheme

private const val HERO_CIRCLE_ACCENT_SIZE_DP = 190
private const val HERO_CIRCLE_ACCENT_X_DP = -40
private const val HERO_CIRCLE_ACCENT_Y_DP = -58
private const val HERO_CIRCLE_SAGE_SIZE_DP = 150
private const val HERO_CIRCLE_SAGE_X_DP = 46
private const val HERO_CIRCLE_SAGE_Y_DP = 44

private const val HERO_COVER_WIDTH_DP = 106
private const val HERO_COVER_HEIGHT_DP = 140
private const val HERO_COVER_RADIUS_DP = 18
private const val HERO_COVER_ELEVATION_DP = 10
private const val HERO_COVER_TILT_DEGREES = 9f
private const val HERO_COVER_LEFT_X_DP = 34
private const val HERO_COVER_LEFT_Y_DP = 96
private const val HERO_COVER_RIGHT_X_DP = -26
private const val HERO_COVER_RIGHT_Y_DP = 98

private const val HERO_COVER_RAISED_WIDTH_DP = 118
private const val HERO_COVER_RAISED_HEIGHT_DP = 156
private const val HERO_COVER_RAISED_RADIUS_DP = 20
private const val HERO_COVER_RAISED_ELEVATION_DP = 14
private const val HERO_COVER_RAISED_Y_DP = 74

private const val HERO_CIRCLE_ACCENT_ALPHA = 0.22f
private const val HERO_CIRCLE_SAGE_ALPHA = 0.28f
private const val HERO_COVER_FLANK_ALPHA = 0.75f

/**
 * The paywall's hero: a tinted band carrying two soft blobs and a fanned stack of three cover
 * placeholders, dissolving into the page at its foot.
 *
 * Drawn outside the content column's window insets on purpose — the band is meant to run behind
 * the status bar, which is the whole reason the screen manages its own insets rather than padding
 * the outer box.
 *
 * The flanking covers are pushed back with [alpha] rather than the design's blur: `Modifier.blur`
 * is a no-op below API 31 and `minSdk` here is 24, so a blur would simply not happen on a third of
 * the supported range. Alpha reads the same on every device.
 */
@Composable
internal fun HeroBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.paywallHero)
            .clipToBounds()
            .background(NativeMindsTheme.colors.heroBand),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = HERO_CIRCLE_ACCENT_X_DP.dp, y = HERO_CIRCLE_ACCENT_Y_DP.dp)
                .size(HERO_CIRCLE_ACCENT_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = HERO_CIRCLE_ACCENT_ALPHA),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = HERO_CIRCLE_SAGE_X_DP.dp, y = HERO_CIRCLE_SAGE_Y_DP.dp)
                .size(HERO_CIRCLE_SAGE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = HERO_CIRCLE_SAGE_ALPHA),
                ),
        )

        HeroCover(
            tiltDegrees = -HERO_COVER_TILT_DEGREES,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = HERO_COVER_LEFT_X_DP.dp, y = HERO_COVER_LEFT_Y_DP.dp),
        )
        HeroCover(
            tiltDegrees = HERO_COVER_TILT_DEGREES,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = HERO_COVER_RIGHT_X_DP.dp, y = HERO_COVER_RIGHT_Y_DP.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = HERO_COVER_RAISED_Y_DP.dp)
                .size(
                    width = HERO_COVER_RAISED_WIDTH_DP.dp,
                    height = HERO_COVER_RAISED_HEIGHT_DP.dp,
                )
                .shadow(
                    elevation = HERO_COVER_RAISED_ELEVATION_DP.dp,
                    shape = RoundedCornerShape(HERO_COVER_RAISED_RADIUS_DP.dp),
                )
                .background(NativeMindsTheme.colors.coverRaised),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(NativeMindsTheme.sizes.paywallHeroFade)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                    ),
                ),
        )
    }
}

/** One of the two flanking covers — same card, mirrored by the sign of [tiltDegrees]. */
@Composable
private fun HeroCover(tiltDegrees: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = HERO_COVER_WIDTH_DP.dp, height = HERO_COVER_HEIGHT_DP.dp)
            .rotate(tiltDegrees)
            .alpha(HERO_COVER_FLANK_ALPHA)
            .shadow(
                elevation = HERO_COVER_ELEVATION_DP.dp,
                shape = RoundedCornerShape(HERO_COVER_RADIUS_DP.dp),
            )
            .background(NativeMindsTheme.colors.cover),
    )
}
