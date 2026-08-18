package com.example.nativeminds.feature.paywall.ui.success

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.theme.NativeMindsTheme

private const val SUCCESS_CIRCLE_SAGE_SIZE_DP = 230
private const val SUCCESS_CIRCLE_SAGE_X_DP = -56
private const val SUCCESS_CIRCLE_SAGE_Y_DP = -72
private const val SUCCESS_CIRCLE_SAGE_ALPHA = 0.24f
private const val SUCCESS_CIRCLE_ACCENT_SIZE_DP = 170
private const val SUCCESS_CIRCLE_ACCENT_X_DP = 64
private const val SUCCESS_CIRCLE_ACCENT_Y_DP = 120
private const val SUCCESS_CIRCLE_ACCENT_ALPHA = 0.18f

/**
 * Two soft blobs behind the confirmation, drawn outside the content column's insets so they run
 * behind the status bar the way the design does. No band and no covers here — the paywall earns
 * that weight because it has to sell something; this screen has already been paid.
 */
@Composable
internal fun SuccessBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = SUCCESS_CIRCLE_SAGE_X_DP.dp, y = SUCCESS_CIRCLE_SAGE_Y_DP.dp)
                .size(SUCCESS_CIRCLE_SAGE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = SUCCESS_CIRCLE_SAGE_ALPHA),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = SUCCESS_CIRCLE_ACCENT_X_DP.dp, y = SUCCESS_CIRCLE_ACCENT_Y_DP.dp)
                .size(SUCCESS_CIRCLE_ACCENT_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = SUCCESS_CIRCLE_ACCENT_ALPHA),
                ),
        )
    }
}
