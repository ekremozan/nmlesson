package com.example.nativeminds.designsystem.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews

/**
 * Hand-authored stroke icons tracing the design's own SVG paths (24×24 viewBox, 2.75 stroke).
 *
 * Drawn with [Canvas] instead of pulling in `material-icons-extended`: the design uses a specific,
 * consistent stroke set (round caps/joins, 2.75 weight) that the generic icon pack doesn't match
 * glyph-for-glyph, and the app only needs about eight of them.
 */
object NativeMindsIcons {
    @Composable
    fun Search(tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawCircle(tint, radius = 7f * s, center = Offset(11f * s, 11f * s), style = stroke)
            drawLine(tint, Offset(16.5f * s, 16.5f * s), Offset(20f * s, 20f * s), stroke.width, stroke.cap)
        }
    }

    @Composable
    fun Person(tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawCircle(tint, radius = 4f * s, center = Offset(12f * s, 8f * s), style = stroke)
            drawArc(
                color = tint,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(4f * s, 13f * s),
                size = Size(16f * s, 16f * s),
                style = stroke,
            )
        }
    }

    /** Cover-art placeholder — a photo icon, shown behind a lesson's cover before art loads. */
    @Composable
    fun ImagePlaceholder(tint: Color, modifier: Modifier = Modifier, size: Dp = 26.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawRoundRect(
                color = tint,
                topLeft = Offset(3f * s, 4f * s),
                size = Size(18f * s, 16f * s),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s),
                style = stroke,
            )
            drawCircle(tint, radius = 1.6f * s, center = Offset(9f * s, 10f * s), style = stroke)
            val horizon = Path().apply {
                moveTo(4f * s, 18f * s)
                lineTo(9f * s, 13.5f * s)
                lineTo(13f * s, 17f * s)
                lineTo(16f * s, 14.5f * s)
                lineTo(20f * s, 18f * s)
            }
            drawPath(horizon, tint, style = stroke)
        }
    }

    @Composable
    fun Headphones(tint: Color, modifier: Modifier = Modifier, size: Dp = 14.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawArc(
                color = tint,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(4f * s, 4f * s),
                size = Size(16f * s, 16f * s),
                style = stroke,
            )
            drawRoundRect(
                color = tint,
                topLeft = Offset(2.5f * s, 14f * s),
                size = Size(5f * s, 7f * s),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f * s),
                style = stroke,
            )
            drawRoundRect(
                color = tint,
                topLeft = Offset(16.5f * s, 14f * s),
                size = Size(5f * s, 7f * s),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f * s),
                style = stroke,
            )
        }
    }

    @Composable
    fun Lock(tint: Color, modifier: Modifier = Modifier, size: Dp = 11.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawRoundRect(
                color = tint,
                topLeft = Offset(4f * s, 10f * s),
                size = Size(16f * s, 11f * s),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f * s),
                style = stroke,
            )
            drawArc(
                color = tint,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(8f * s, 3f * s),
                size = Size(8f * s, 8f * s),
                style = stroke,
            )
        }
    }

    @Composable
    fun Home(tint: Color, modifier: Modifier = Modifier, size: Dp = 23.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            val roof = Path().apply {
                moveTo(4f * s, 11f * s)
                lineTo(12f * s, 4f * s)
                lineTo(20f * s, 11f * s)
            }
            val walls = Path().apply {
                moveTo(6f * s, 10f * s)
                lineTo(6f * s, 19f * s)
                lineTo(18f * s, 19f * s)
                lineTo(18f * s, 10f * s)
            }
            drawPath(roof, tint, style = stroke)
            drawPath(walls, tint, style = stroke)
        }
    }

    @Composable
    fun Library(tint: Color, modifier: Modifier = Modifier, size: Dp = 23.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            val leftBar = Path().apply {
                moveTo(5f * s, 4f * s)
                lineTo(10f * s, 4f * s)
                lineTo(10f * s, 20f * s)
                lineTo(5f * s, 20f * s)
                close()
            }
            val rightBar = Path().apply {
                moveTo(13f * s, 4f * s)
                lineTo(18f * s, 4f * s)
                lineTo(20f * s, 20f * s)
                lineTo(13f * s, 20f * s)
                close()
            }
            drawPath(leftBar, tint, style = stroke)
            drawPath(rightBar, tint, style = stroke)
        }
    }

    @Composable
    fun Sparkle(tint: Color, modifier: Modifier = Modifier, size: Dp = 23.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            val bigStar = Path().apply {
                moveTo(12f * s, 3f * s)
                lineTo(13.8f * s, 7.7f * s)
                lineTo(18.5f * s, 9f * s)
                lineTo(13.8f * s, 10.8f * s)
                lineTo(12f * s, 15.5f * s)
                lineTo(10.2f * s, 10.8f * s)
                lineTo(5.5f * s, 9f * s)
                lineTo(10.2f * s, 7.7f * s)
                close()
            }
            val smallStar = Path().apply {
                moveTo(18f * s, 17.5f * s)
                lineTo(18.8f * s, 19.5f * s)
                lineTo(20.8f * s, 20.3f * s)
                lineTo(18.8f * s, 21.1f * s)
                lineTo(18f * s, 23.1f * s)
                lineTo(17.2f * s, 21.1f * s)
                lineTo(15.2f * s, 20.3f * s)
                lineTo(17.2f * s, 19.5f * s)
                close()
            }
            drawPath(bigStar, tint, style = stroke)
            drawPath(smallStar, tint, style = stroke)
        }
    }

    /** Back chevron in the reader's top bar. */
    @Composable
    fun ChevronLeft(tint: Color, modifier: Modifier = Modifier, size: Dp = 21.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            val chevron = Path().apply {
                moveTo(15f * s, 5f * s)
                lineTo(8f * s, 12f * s)
                lineTo(15f * s, 19f * s)
            }
            drawPath(chevron, tint, style = stroke)
        }
    }

    /** Overflow menu — three stacked dots. */
    @Composable
    fun MoreVertical(tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            listOf(6f, 12f, 18f).forEach { y ->
                drawCircle(tint, radius = stroke.width / 2f, center = Offset(12f * s, y * s))
            }
        }
    }

    /** Filled play triangle inside the listen control. */
    @Composable
    fun Play(tint: Color, modifier: Modifier = Modifier, size: Dp = 17.dp) {
        StrokeCanvas(modifier, size) { s, _ ->
            val triangle = Path().apply {
                moveTo(8f * s, 5.5f * s)
                lineTo(19f * s, 12f * s)
                lineTo(8f * s, 18.5f * s)
                close()
            }
            drawPath(triangle, tint)
        }
    }

    /** Two filled bars — the listen control's Playing state, toggled with [Play]. */
    @Composable
    fun Pause(tint: Color, modifier: Modifier = Modifier, size: Dp = 17.dp) {
        StrokeCanvas(modifier, size) { s, _ ->
            drawRect(tint, topLeft = Offset(6f * s, 5f * s), size = Size(4f * s, 14f * s))
            drawRect(tint, topLeft = Offset(14f * s, 5f * s), size = Size(4f * s, 14f * s))
        }
    }

    /** Benefit check in the premium sheet. */
    @Composable
    fun Check(tint: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            val tick = Path().apply {
                moveTo(5f * s, 13f * s)
                lineTo(9.5f * s, 17.5f * s)
                lineTo(19f * s, 7f * s)
            }
            drawPath(tick, tint, style = stroke)
        }
    }

    /** Download/offline glyph — arrow into a tray. */
    @Composable
    fun Download(tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawLine(tint, Offset(12f * s, 4f * s), Offset(12f * s, 14f * s), stroke.width, stroke.cap)
            val arrow = Path().apply {
                moveTo(8f * s, 11f * s)
                lineTo(12f * s, 15f * s)
                lineTo(16f * s, 11f * s)
            }
            drawPath(arrow, tint, style = stroke)
            drawLine(tint, Offset(4f * s, 18f * s), Offset(20f * s, 18f * s), stroke.width, stroke.cap)
        }
    }

    @Composable
    fun Close(tint: Color, modifier: Modifier = Modifier, size: Dp = 13.dp) {
        StrokeCanvas(modifier, size) { s, stroke ->
            drawLine(tint, Offset(5f * s, 5f * s), Offset(19f * s, 19f * s), stroke.width, stroke.cap)
            drawLine(tint, Offset(19f * s, 5f * s), Offset(5f * s, 19f * s), stroke.width, stroke.cap)
        }
    }
}

@Composable
private fun StrokeCanvas(
    modifier: Modifier,
    size: Dp,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.(scale: Float, stroke: Stroke) -> Unit,
) {
    Canvas(modifier.size(size)) {
        val s = this.size.width / 24f
        val stroke = Stroke(
            width = 2.75f * s,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        onDraw(s, stroke)
    }
}

/**
 * Gallery of the whole icon set at one size — the fastest way to spot a path that broke, and the
 * preview that covers [StrokeCanvas] (it has no visual of its own).
 */
@ThemePreviews
@Composable
private fun NativeMindsIconsPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val tint = MaterialTheme.colorScheme.onSurface
            NativeMindsIcons.Search(tint = tint, size = 24.dp)
            NativeMindsIcons.Person(tint = tint, size = 24.dp)
            NativeMindsIcons.ImagePlaceholder(tint = tint, size = 24.dp)
            NativeMindsIcons.Headphones(tint = tint, size = 24.dp)
            NativeMindsIcons.Lock(tint = tint, size = 24.dp)
            NativeMindsIcons.Home(tint = tint, size = 24.dp)
            NativeMindsIcons.Library(tint = tint, size = 24.dp)
            NativeMindsIcons.Sparkle(tint = tint, size = 24.dp)
            NativeMindsIcons.Close(tint = tint, size = 24.dp)
            NativeMindsIcons.ChevronLeft(tint = tint, size = 24.dp)
            NativeMindsIcons.MoreVertical(tint = tint, size = 24.dp)
            NativeMindsIcons.Play(tint = tint, size = 24.dp)
            NativeMindsIcons.Pause(tint = tint, size = 24.dp)
            NativeMindsIcons.Check(tint = tint, size = 24.dp)
            NativeMindsIcons.Download(tint = tint, size = 24.dp)
        }
    }
}
