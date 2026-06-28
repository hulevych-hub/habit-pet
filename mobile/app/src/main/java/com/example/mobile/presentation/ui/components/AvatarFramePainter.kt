package com.example.mobile.presentation.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import com.example.mobile.domain.AvatarFrameConfig

/**
 * Draws a premium glowing frame border around the pet showcase area.
 *
 * The frame is composed of three layers:
 * 1. Outer glow — soft gradient spread behind the border
 * 2. Main stroke — gradient line from innerColor to outerColor
 * 3. Inner shimmer — subtle accent highlight on the inner edge
 */
@Composable
fun AvatarFrameOverlay(
    frame: AvatarFrameConfig.FrameDefinition,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val innerColor = Color(frame.innerColor)
    val outerColor = Color(frame.outerColor)
    val accentColor = Color(frame.accentColor)
    val glowStrength = frame.glowStrength

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val halfWidth = width / 2f
        val halfHeight = height / 2f

        // Resolve actual corner radius from shape
        val resolvedCorners = run {
            val outline = shape.createOutline(
                size = size,
                layoutDirection = LayoutDirection.Ltr,
                density = density
            )
            when (outline) {
                is Outline.Rounded -> {
                    val rr = outline.roundRect
                    CornerRadius(
                        rr.topLeftCornerRadius.x.coerceAtMost(rr.topLeftCornerRadius.y),
                        rr.topLeftCornerRadius.x.coerceAtMost(rr.topLeftCornerRadius.y)
                    )
                }
                else -> CornerRadius(34.dp.toPx(), 34.dp.toPx())
            }
        }
        val cr = resolvedCorners.x

        // Layer 1: Outer glow — multiple soft expanding strokes
        val glowPasses = 5
        val glowBaseSpread = 6.dp.toPx()
        for (i in glowPasses downTo 1) {
            val fraction = i.toFloat() / glowPasses
            val spread = glowBaseSpread * fraction
            val alpha = glowStrength * (1f - fraction) * 0.35f
            val glowStroke = Stroke(width = 2.dp.toPx() + spread * 2f)
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        outerColor.copy(alpha = alpha),
                        outerColor.copy(alpha = alpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(halfWidth, halfHeight),
                    radius = halfWidth.coerceAtLeast(halfHeight)
                ),
                cornerRadius = CornerRadius(cr + spread, cr + spread),
                style = glowStroke,
                topLeft = Offset(-spread, -spread),
                size = Size(width + spread * 2, height + spread * 2)
            )
        }

        // Layer 2: Main gradient stroke
        val mainStrokeWidth = 2.5.dp.toPx()
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(innerColor, outerColor, innerColor),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            ),
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = mainStrokeWidth)
        )

        // Layer 3: Inner shimmer accent
        val shimmerInset = mainStrokeWidth + 1.dp.toPx()
        val shimmerStrokeWidth = 1.dp.toPx()
        drawRoundRect(
            color = accentColor.copy(alpha = 0.30f),
            cornerRadius = CornerRadius(
                (cr - shimmerInset).coerceAtLeast(0f),
                (cr - shimmerInset).coerceAtLeast(0f)
            ),
            style = Stroke(width = shimmerStrokeWidth),
            topLeft = Offset(shimmerInset, shimmerInset),
            size = Size(width - shimmerInset * 2, height - shimmerInset * 2)
        )
    }
}
