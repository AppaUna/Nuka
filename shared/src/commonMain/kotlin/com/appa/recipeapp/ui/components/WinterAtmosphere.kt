package com.appa.recipeapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class WinterCardStyle {
    Large,
    Medium,
    ListRow,
}

class DynamicSnowflake(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val baseVy: Float,
    val radius: Float,
    val driftAmp1: Float,
    val driftAmp2: Float,
    val freq1: Float,
    val freq2: Float,
    val phase1: Float,
    val phase2: Float,
    val alpha: Float,
    val isCrystal: Boolean,
    val spinSpeed: Float,
)

class SnowSimulationState(
    val flakes: List<DynamicSnowflake>,
)

@Composable
fun rememberSnowSimulationState(flakeCount: Int = 34): SnowSimulationState {
    return remember(flakeCount) {
        val rng = Random(20261120)
        val list = List(flakeCount) { i ->
            val layer = i % 3
            val baseVy = when (layer) {
                0 -> 14f + rng.nextFloat() * 10f
                1 -> 24f + rng.nextFloat() * 14f
                else -> 38f + rng.nextFloat() * 18f
            }
            DynamicSnowflake(
                x = rng.nextFloat() * 1080f,
                y = rng.nextFloat() * 2200f,
                vx = 0f,
                vy = baseVy,
                baseVy = baseVy,
                radius = when (layer) {
                    0 -> 1.8f + rng.nextFloat() * 0.5f
                    1 -> 2.8f + rng.nextFloat() * 0.8f
                    else -> 4.0f + rng.nextFloat() * 1.1f
                },
                driftAmp1 = (6f + rng.nextFloat() * 12f) * (if (rng.nextBoolean()) 1f else -1f),
                driftAmp2 = (4f + rng.nextFloat() * 9f) * (if (rng.nextBoolean()) 1f else -1f),
                freq1 = 0.31f + rng.nextFloat() * 0.47f,
                freq2 = 0.73f + rng.nextFloat() * 0.61f,
                phase1 = rng.nextFloat() * 2f * PI.toFloat(),
                phase2 = rng.nextFloat() * 2f * PI.toFloat(),
                alpha = when (layer) {
                    0 -> 0.16f + rng.nextFloat() * 0.06f
                    1 -> 0.28f + rng.nextFloat() * 0.08f
                    else -> 0.42f + rng.nextFloat() * 0.10f
                },
                isCrystal = layer == 2 && rng.nextFloat() > 0.45f,
                spinSpeed = (0.25f + rng.nextFloat() * 0.45f) * (if (rng.nextBoolean()) 1f else -1f),
            )
        }
        SnowSimulationState(list)
    }
}

fun Modifier.observeSnowPointer(
    snowState: SnowSimulationState,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    return this.pointerInput(snowState) {
        awaitEachGesture {
            var prevPos: Offset? = null
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val active = event.changes.firstOrNull { it.pressed } ?: break
                val pos = active.position
                val prev = prevPos
                if (prev != null) {
                    val dx = pos.x - prev.x
                    val dy = pos.y - prev.y
                    for (f in snowState.flakes) {
                        val dist = hypot(f.x - pos.x, f.y - pos.y)
                        if (dist < 170f && dist > 1f) {
                            val factor = 1f - dist / 170f
                            val repelX = (f.x - pos.x) / dist
                            val repelY = (f.y - pos.y) / dist
                            f.vx += (dx * 3.4f + repelX * 65f) * factor
                            f.vy += (dy * 2.4f + repelY * 45f) * factor
                        }
                    }
                }
                prevPos = pos
            }
        }
    }
}

/**
 * Глубокий ночной фон: сапфировая полночь, мягкое северное сияние и тихие звёзды.
 * Верхний край остаётся прозрачным для бесшовной стыковки со шторкой iOS PWA.
 */
@Composable
fun WinterConservatoryBackdrop(
    scrollParallax: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "nocturnal_aurora_bg")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora_drift",
    )

    val stars = remember {
        List(24) { idx ->
            val sx = ((idx * 73 + 19) % 100) / 100f
            val sy = 0.14f + ((idx * 41 + 13) % 82) / 100f
            val sr = 1.0f + (idx % 3) * 0.5f
            val phase = idx * 0.9f
            Triple(Offset(sx, sy), sr, phase)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val pShift = (scrollParallax * 0.20f).coerceIn(-150f, 150f)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF071224).copy(alpha = 0.78f),
                    Color(0xFF0A1930).copy(alpha = 0.90f),
                    Color(0xFF071122).copy(alpha = 0.96f),
                    Color(0xFF050B16),
                ),
                startY = h * 0.08f,
                endY = h,
            ),
        )

        val nebulaCenter = Offset(
            x = w * (0.78f + 0.05f * sin(drift)),
            y = h * 0.34f - pShift * 0.45f,
        )
        val nebulaRadius = (w * 0.85f).coerceAtLeast(10f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6366F1).copy(alpha = 0.12f),
                    Color(0xFFA78BFA).copy(alpha = 0.06f),
                    Color.Transparent,
                ),
                center = nebulaCenter,
                radius = nebulaRadius,
            ),
            radius = nebulaRadius,
            center = nebulaCenter,
        )

        val auroraCenter = Offset(
            x = w * (0.20f + 0.06f * cos(drift * 0.85f)),
            y = h * 0.56f - pShift * 0.35f,
        )
        val auroraRadius = (w * 0.88f).coerceAtLeast(10f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.10f),
                    Color(0xFF2DD4BF).copy(alpha = 0.05f),
                    Color.Transparent,
                ),
                center = auroraCenter,
                radius = auroraRadius,
            ),
            radius = auroraRadius,
            center = auroraCenter,
        )

        for (i in stars.indices) {
            val (posFrac, starR, phase) = stars[i]
            val twinkle = 0.25f + 0.75f * (0.5f + 0.5f * sin(drift * 2.1f + phase))
            val sx = w * posFrac.x
            val sy = (h * posFrac.y - pShift * 0.16f).coerceIn(h * 0.12f, h * 0.94f)
            val starColor = if (i % 3 == 0) Color(0xFFBAE6FD) else Color(0xFFE0F2FE)
            drawCircle(
                color = starColor.copy(alpha = 0.32f * twinkle),
                radius = starR,
                center = Offset(sx, sy),
            )
        }
    }
}

/**
 * Аккуратная одинарная ледяная фаска для карточек рецептов:
 * НЕ затемняет и НЕ обрезает фотографию блюда.
 */
fun Modifier.winterRecipeCard(
    enabled: Boolean,
    style: WinterCardStyle = WinterCardStyle.Medium,
    cornerRadius: Dp = 16.dp,
    highlightPhase: Float = 0f,
    isFocused: Boolean = false,
): Modifier {
    if (!enabled) return this
    return this.drawWithContent {
        drawContent()

        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@drawWithContent

        val r = cornerRadius.toPx()
        val cr = CornerRadius(r, r)
        val shimmer = 0.5f + 0.5f * sin(highlightPhase)

        // Одинарная тонкая ледяная рамка по периметру
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = if (isFocused) {
                    listOf(
                        Color(0xFFE0F2FE).copy(alpha = 0.62f),
                        Color(0xFF67E8F9).copy(alpha = 0.44f),
                        Color(0xFF818CF8).copy(alpha = 0.34f),
                        Color(0xFFBAE6FD).copy(alpha = 0.46f),
                    )
                } else {
                    listOf(
                        Color(0xFFE0F2FE).copy(alpha = 0.34f + 0.10f * shimmer),
                        Color(0xFF67E8F9).copy(alpha = 0.20f),
                        Color(0xFF818CF8).copy(alpha = 0.16f),
                        Color(0xFF38BDF8).copy(alpha = 0.24f),
                    )
                },
                start = Offset.Zero,
                end = Offset(w, h),
            ),
            cornerRadius = cr,
            style = Stroke(width = if (isFocused) 1.5f else 1.1f),
        )
    }
}

/**
 * Непериодический естественный снегопад.
 * Каждая снежинка движется по уникальной плавной траектории без зацикленных волн.
 */
@Composable
fun InteractiveSnowfallLayer(
    snowState: SnowSimulationState,
    modifier: Modifier = Modifier,
    topFadeStartFraction: Float = 0f,
) {
    var frameTick by remember { mutableLongStateOf(0L) }
    var elapsedNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(snowState) {
        var lastNanos = 0L
        var totalNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    totalNanos += (now - lastNanos)
                    val t = totalNanos / 1_000_000_000f

                    for (f in snowState.flakes) {
                        // Непериодическая сумма двух несоизмеримых гармоник — никакого зацикливания
                        val gustVx = sin(t * f.freq1 + f.phase1) * f.driftAmp1 +
                                cos(t * f.freq2 + f.phase2) * f.driftAmp2
                        val targetVy = f.baseVy * (0.86f + 0.24f * sin(t * f.freq2 * 0.7f + f.phase1))

                        f.vx += (gustVx - f.vx) * (1.8f * dt).coerceAtMost(1f)
                        f.vy += (targetVy - f.vy) * (1.6f * dt).coerceAtMost(1f)
                        f.x += f.vx * dt
                        f.y += f.vy * dt
                    }
                    elapsedNanos = totalNanos
                    frameTick++
                }
                lastNanos = now
            }
        }
    }

    Canvas(modifier = modifier) {
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick
        val t = elapsedNanos / 1_000_000_000f
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val startY = h * topFadeStartFraction.coerceIn(0f, 0.85f)
        val fadeBand = (h * 0.09f).coerceAtLeast(28f)

        for (f in snowState.flakes) {
            if (f.x < -20f) f.x += w + 40f
            if (f.x > w + 20f) f.x -= w + 40f
            if (f.y > h + 20f) {
                f.y = startY
                f.x = Random.nextFloat() * w
            }
            if (f.y < startY - 12f) {
                f.y = startY
            }

            val spawnFade = if (topFadeStartFraction <= 0f) {
                1f
            } else {
                ((f.y - startY) / fadeBand).coerceIn(0f, 1f)
            }
            if (spawnFade <= 0.01f) continue

            val color = Color(0xFFE0F2FE).copy(alpha = f.alpha * spawnFade)
            if (f.isCrystal) {
                drawSnowCrystal(
                    center = Offset(f.x, f.y),
                    radius = f.radius,
                    rotation = f.phase1 + t * f.spinSpeed,
                    color = color,
                )
            } else {
                val glowRadius = (f.radius * 1.5f).coerceAtLeast(1.5f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = color.alpha * 0.28f),
                            Color.Transparent,
                        ),
                        center = Offset(f.x, f.y),
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = Offset(f.x, f.y),
                )
            }
        }
    }
}

private fun DrawScope.drawSnowCrystal(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color,
) {
    val stroke = (radius * 0.22f).coerceAtLeast(0.95f)
    for (k in 0 until 3) {
        val angle = rotation + (k * PI / 3.0).toFloat()
        val dx = cos(angle) * radius
        val dy = sin(angle) * radius
        drawLine(
            color = color,
            start = Offset(center.x - dx, center.y - dy),
            end = Offset(center.x + dx, center.y + dy),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        for (dir in listOf(-1f, 1f)) {
            val bx = center.x + dx * 0.56f * dir
            val by = center.y + dy * 0.56f * dir
            val bLen = radius * 0.34f
            for (side in listOf(-0.55f, 0.55f)) {
                val bAngle = angle + side + (if (dir < 0f) PI.toFloat() else 0f)
                drawLine(
                    color = color.copy(alpha = color.alpha * 0.85f),
                    start = Offset(bx, by),
                    end = Offset(bx + cos(bAngle) * bLen, by + sin(bAngle) * bLen),
                    strokeWidth = stroke * 0.8f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun ScreenBottomSnowdrift(
    scrollParallax: Float,
    lightPhase: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val farShift = sin(scrollParallax * 0.010f) * 14f
        val midShift = cos(scrollParallax * 0.015f) * 20f
        val nearShift = sin(scrollParallax * 0.021f + 0.8f) * 26f
        val crestShimmer = 0.28f + 0.12f * sin(lightPhase)

        val farDrift = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.28f)
            cubicTo(
                w * 0.26f + farShift, h * 0.10f,
                w * 0.52f + farShift, h * 0.42f,
                w * 0.78f + farShift, h * 0.16f,
            )
            cubicTo(
                w * 0.90f, h * 0.06f,
                w * 0.96f, h * 0.22f,
                w, h * 0.16f,
            )
            lineTo(w, h)
            close()
        }
        drawPath(
            path = farDrift,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF818CF8).copy(alpha = 0.15f),
                    Color(0xFF38BDF8).copy(alpha = 0.07f),
                    Color.Transparent,
                ),
                startY = h * 0.06f,
                endY = h,
            ),
        )

        val midDrift = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.42f)
            cubicTo(
                w * 0.22f + midShift, h * 0.22f,
                w * 0.46f + midShift, h * 0.52f,
                w * 0.70f + midShift, h * 0.26f,
            )
            cubicTo(
                w * 0.85f, h * 0.12f,
                w * 0.94f, h * 0.34f,
                w, h * 0.24f,
            )
            lineTo(w, h)
            close()
        }
        drawPath(
            path = midDrift,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBAE6FD).copy(alpha = 0.22f),
                    Color(0xFF38BDF8).copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                startY = h * 0.14f,
                endY = h,
            ),
        )

        val nearDrift = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.54f)
            cubicTo(
                w * 0.18f + nearShift, h * 0.36f,
                w * 0.40f + nearShift, h * 0.62f,
                w * 0.64f + nearShift, h * 0.40f,
            )
            cubicTo(
                w * 0.82f, h * 0.24f,
                w * 0.92f, h * 0.44f,
                w, h * 0.36f,
            )
            lineTo(w, h)
            close()
        }
        drawPath(
            path = nearDrift,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8FAFC).copy(alpha = 0.25f),
                    Color(0xFFBAE6FD).copy(alpha = 0.12f),
                    Color.Transparent,
                ),
                startY = h * 0.24f,
                endY = h,
            ),
        )

        val crestPath = Path().apply {
            moveTo(0f, h * 0.54f)
            cubicTo(
                w * 0.18f + nearShift, h * 0.36f,
                w * 0.40f + nearShift, h * 0.62f,
                w * 0.64f + nearShift, h * 0.40f,
            )
            cubicTo(
                w * 0.82f, h * 0.24f,
                w * 0.92f, h * 0.44f,
                w, h * 0.36f,
            )
        }
        drawPath(
            path = crestPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFBAE6FD).copy(alpha = 0.12f),
                    Color(0xFFFFFFFF).copy(alpha = crestShimmer),
                    Color(0xFF67E8F9).copy(alpha = 0.28f),
                ),
            ),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round),
        )
    }
}

/**
 * Аккуратная тактильная отдача для кнопок и чипов (вместо квадратных частиц):
 * мягкое упругое сжатие + ледяной световой отклик внутри контура элемента.
 */
fun Modifier.festiveTapBurst(
    enabled: Boolean,
    cornerRadius: Dp = 18.dp,
): Modifier = composed {
    if (!enabled) return@composed this

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "winter_tap_scale",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.22f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 90 else 260),
        label = "winter_tap_glow",
    )

    this
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitPointerEvent(PointerEventPass.Initial)
                if (down.changes.any { it.pressed }) {
                    isPressed = true
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.none { it.pressed }) break
                    }
                    isPressed = false
                }
            }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            if (glowAlpha > 0.005f) {
                val r = cornerRadius.toPx()
                val cr = CornerRadius(r, r)
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF67E8F9).copy(alpha = glowAlpha),
                            Color(0xFF818CF8).copy(alpha = glowAlpha * 0.45f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = (size.width.coerceAtLeast(size.height) * 0.65f).coerceAtLeast(8f),
                    ),
                    cornerRadius = cr,
                )
            }
        }
}

/**
 * Тонкая крио-стеклянная кромка для чипов и плашек.
 */
fun Modifier.arcticGlassCard(
    enabled: Boolean,
    cornerRadius: Dp = 14.dp,
    highlightPhase: Float = 0f,
    auroraAccent: Boolean = false,
): Modifier {
    if (!enabled) return this
    return this.drawWithContent {
        drawContent()
        val r = cornerRadius.toPx()
        val cr = CornerRadius(r, r)
        val shimmer = 0.5f + 0.5f * sin(highlightPhase)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = if (auroraAccent) {
                    listOf(
                        Color(0xFF67E8F9).copy(alpha = 0.48f + 0.12f * shimmer),
                        Color(0xFF818CF8).copy(alpha = 0.30f),
                        Color(0xFF38BDF8).copy(alpha = 0.36f),
                    )
                } else {
                    listOf(
                        Color(0xFFE0F2FE).copy(alpha = 0.32f + 0.10f * shimmer),
                        Color(0xFF67E8F9).copy(alpha = 0.18f),
                        Color(0xFF38BDF8).copy(alpha = 0.22f),
                    )
                },
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            cornerRadius = cr,
            style = Stroke(width = 1.1f),
        )
    }
}