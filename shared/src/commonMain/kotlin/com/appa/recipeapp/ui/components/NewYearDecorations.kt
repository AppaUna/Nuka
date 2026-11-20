package com.appa.recipeapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class FlakeConfig(
    val xSeed: Float,
    val yOffset: Float,
    val speedPxPerSec: Float,
    val radius: Float,
    val swayAmp: Float,
    val swayFreq: Float,
    val phase: Float,
    val alpha: Float,
    val hasBranches: Boolean,
)

private val GARLAND_PALETTE = listOf(
    Color(0xFF5CE1E6),
    Color(0xFF93C5FD),
    Color(0xFF38D399),
    Color(0xFFFF4D6D),
)

@Composable
fun SnowfallOverlay(
    modifier: Modifier = Modifier,
    flakeCount: Int = 28,
) {
    val flakes = remember(flakeCount) {
        List(flakeCount) { i ->
            val layer = i % 3
            val h = ((i * 53 + 17) % 100) / 100f
            FlakeConfig(
                xSeed = ((i * 37 + 11) % 100) / 100f,
                yOffset = ((i * 67 + 29) % 100) / 100f,
                speedPxPerSec = when (layer) {
                    0 -> 18f + h * 9f
                    1 -> 30f + h * 12f
                    else -> 44f + h * 15f
                },
                radius = when (layer) {
                    0 -> 2.8f
                    1 -> 4.6f
                    else -> 6.2f
                },
                swayAmp = when (layer) {
                    0 -> 6f
                    1 -> 11f
                    else -> 17f
                },
                swayFreq = 0.0011f + (i % 7) * 0.00022f,
                phase = (i * 0.9f) % (2f * PI.toFloat()),
                alpha = when (layer) {
                    0 -> 0.20f
                    1 -> 0.36f
                    else -> 0.52f
                },
                hasBranches = layer >= 1,
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "snowfall_loop")
    val elapsedSec by transition.animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "snow_elapsed_sec",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        for (flake in flakes) {
            val travel = elapsedSec * flake.speedPxPerSec
            val startY = flake.yOffset * (h + flake.radius * 4f) - flake.radius * 2f
            val totalSpan = h + flake.radius * 6f
            val y = ((startY + travel) % totalSpan + totalSpan) % totalSpan - flake.radius * 2f

            val cycleIndex = ((startY + travel) / totalSpan).toInt()
            val driftX = sin(elapsedSec * flake.swayFreq * 1000f + flake.phase) * flake.swayAmp
            val baseX = ((flake.xSeed + cycleIndex * 0.19f) % 1f + 1f) % 1f * w
            val x = ((baseX + driftX) % w + w) % w

            drawCrystalFlake(
                center = Offset(x, y),
                radius = flake.radius,
                color = Color(0xFFEAF7FF).copy(alpha = flake.alpha),
                hasBranches = flake.hasBranches,
            )
        }
    }
}

private fun DrawScope.drawCrystalFlake(
    center: Offset,
    radius: Float,
    color: Color,
    hasBranches: Boolean,
) {
    val strokeW = (radius * 0.22f).coerceAtLeast(1f)
    for (k in 0 until 3) {
        val angle = (k * PI / 3.0).toFloat()
        val dx = cos(angle) * radius
        val dy = sin(angle) * radius
        drawLine(
            color = color,
            start = Offset(center.x - dx, center.y - dy),
            end = Offset(center.x + dx, center.y + dy),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
        if (hasBranches) {
            val tipX = center.x + dx * 0.62f
            val tipY = center.y + dy * 0.62f
            val branchLen = radius * 0.32f
            for (side in listOf(-0.55f, 0.55f)) {
                val bAngle = angle + side
                drawLine(
                    color = color.copy(alpha = color.alpha * 0.85f),
                    start = Offset(tipX, tipY),
                    end = Offset(tipX + cos(bAngle) * branchLen, tipY + sin(bAngle) * branchLen),
                    strokeWidth = strokeW * 0.85f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

fun Modifier.searchBarGarland(
    enabled: Boolean,
    wavePhase: Float,
    bulbCount: Int = 9,
): Modifier {
    if (!enabled) return this
    return this.drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f || bulbCount < 2) return@drawWithContent

        val r = h * 0.5f
        val leftX = r * 0.9f
        val rightX = w - r * 0.9f
        val span = (rightX - leftX) / (bulbCount - 1)

        val wirePath = Path().apply {
            moveTo(leftX, 1.5f)
            for (i in 0 until bulbCount - 1) {
                val x0 = leftX + i * span
                val x1 = leftX + (i + 1) * span
                val midX = (x0 + x1) * 0.5f
                quadraticTo(midX, 8.5f, x1, 1.5f)
            }
        }

        drawPath(
            path = wirePath,
            color = Color(0xFFB8D4EC).copy(alpha = 0.45f),
            style = Stroke(width = 1.4f, cap = StrokeCap.Round),
        )

        for (i in 0 until bulbCount) {
            val cx = leftX + i * span
            val cy = 6.0f
            val color = GARLAND_PALETTE[i % GARLAND_PALETTE.size]
            val pulse = 0.5f + 0.5f * (sin(wavePhase + i * 0.8f) * 0.7f + cos(wavePhase * 0.6f - i * 0.5f) * 0.3f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.34f + 0.22f * pulse),
                        color.copy(alpha = 0.08f * pulse),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = 15f + 5f * pulse,
                ),
                radius = 15f + 5f * pulse,
                center = Offset(cx, cy),
            )
            drawCircle(
                color = color.copy(alpha = 0.68f + 0.32f * pulse),
                radius = 4.2f + 0.8f * pulse,
                center = Offset(cx, cy + 1.2f),
            )
        }
    }
}

fun Modifier.frostCard(
    enabled: Boolean,
    cornerRadius: Dp = 16.dp,
): Modifier {
    if (!enabled) return this
    return this.drawWithContent {
        drawContent()
        val r = cornerRadius.toPx()
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFBAE6FD).copy(alpha = 0.36f),
                Color(0xFF5CE1E6).copy(alpha = 0.12f),
                Color(0xFF93C5FD).copy(alpha = 0.28f),
            ),
        )
        drawRoundRect(
            brush = borderBrush,
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = 1.6f),
        )
    }
}

fun Modifier.frostHorizontalCard(
    enabled: Boolean,
    cornerRadius: Dp = 14.dp,
): Modifier {
    if (!enabled) return this
    return this.drawWithContent {
        drawContent()
        val r = cornerRadius.toPx()
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF5CE1E6).copy(alpha = 0.42f),
                Color(0xFF93C5FD).copy(alpha = 0.18f),
                Color(0xFFFF4D6D).copy(alpha = 0.30f),
            ),
        )
        drawRoundRect(
            brush = borderBrush,
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = 1.6f),
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF5CE1E6).copy(alpha = 0.08f),
                    Color.Transparent,
                    Color(0xFF93C5FD).copy(alpha = 0.07f),
                ),
            ),
            cornerRadius = CornerRadius(r, r),
        )
    }
}