package com.example.nativeminds.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii.
 *
 * The design system softens everything and takes small controls all the way to a pill — that
 * "rounded frame" is what makes the product read as warm rather than clinical, so [Pill] is used
 * for every chip, button and input rather than a small radius.
 */
internal val NativeMindsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    // Story cards and cover art.
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp),
    // Sheets and dialogs.
    extraLarge = RoundedCornerShape(32.dp),
)

/** Fully rounded — chips, buttons, search field, badges. */
val Pill = RoundedCornerShape(percent = 50)
