package com.appa.recipeapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.data.DailyQuote
import com.appa.recipeapp.ui.theme.NewYearTheme
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QuoteCard(
    quote: DailyQuote,
    gusteauImage: DrawableResource,
    modifier: Modifier = Modifier,
) {
    val isNewYear = MaterialTheme.colorScheme.primary == NewYearTheme.colors.accent
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "quote_editorial_glass")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isNewYear) 7200 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "quote_sweep",
    )

    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    val auroraPulse = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (isNewYear) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPoint = offset
                            scope.launch {
                                auroraPulse.snapTo(1f)
                                auroraPulse.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 2600, easing = LinearEasing),
                                )
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        Canvas(
            modifier = Modifier.matchParentSize(),
        ) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val corner = 22.dp.toPx()
            val cornerRadius = CornerRadius(corner, corner)
            val peakPos = (animatedOffset * 3.4f - 1.2f) * w
            val pulse = auroraPulse.value
            val tap = touchPoint

            if (isNewYear) {
                // 1. Ночное крио-стекло с переходом от сапфира к сумеречному индиго
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0A1B30).copy(alpha = 0.72f),
                            Color(0xFF0E2340).copy(alpha = 0.62f),
                            Color(0xFF141B38).copy(alpha = 0.70f),
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h),
                    ),
                    cornerRadius = cornerRadius,
                )

                // Диагональный атласный перелив северного сияния
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE).copy(alpha = 0.08f),
                            Color.Transparent,
                            Color(0xFF818CF8).copy(alpha = 0.08f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w * 0.85f, h),
                    ),
                    cornerRadius = cornerRadius,
                )

                // 2. Гравированная кристаллическая снежинка-розетка справа
                val rosetteCenter = Offset(w * 0.86f, h * 0.50f)
                val rosetteAlpha = 0.10f + 0.16f * pulse
                drawEtchedCrystalRosette(
                    center = rosetteCenter,
                    outerRadius = h * 0.56f,
                    rotation = animatedOffset * (PI.toFloat() / 3f),
                    alpha = rosetteAlpha,
                )

                // 3. Волна северного сияния при нажатии на карточку
                if (tap != null && pulse > 0.01f) {
                    val rippleRadius = (110f + (1f - pulse) * 140f).coerceAtLeast(10f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF67E8F9).copy(alpha = 0.24f * pulse),
                                Color(0xFF818CF8).copy(alpha = 0.12f * pulse),
                                Color.Transparent,
                            ),
                            center = tap,
                            radius = rippleRadius,
                        ),
                        radius = rippleRadius,
                        center = tap,
                    )
                }

                // 4. Кристаллическая фаска (серебристый лёд + ледяной циан + индиго)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE).copy(alpha = 0.50f),
                            Color(0xFF67E8F9).copy(alpha = 0.32f),
                            Color(0xFF818CF8).copy(alpha = 0.34f),
                            Color(0xFF38BDF8).copy(alpha = 0.32f),
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h),
                    ),
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 1.6f),
                )
            } else {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 1.5f),
                )
            }

            // Бегущий ледяной световой блик по периметру рамки
            val peakColor = if (isNewYear) {
                Color(0xFFBAE6FD).copy(alpha = 0.88f)
            } else {
                Color.White.copy(alpha = 0.55f)
            }
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        peakColor,
                        Color.Transparent,
                    ),
                    start = Offset(peakPos - 105f, 0f),
                    end = Offset(peakPos + 105f, 0f),
                ),
                cornerRadius = cornerRadius,
                style = Stroke(width = if (isNewYear) 2.0f else 1.5f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(gusteauImage),
                contentDescription = "Огюст Гюсто",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(52.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "«${quote.text}»",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "— ${quote.author}",
                        color = if (isNewYear) Color(0xFF67E8F9) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEtchedCrystalRosette(
    center: Offset,
    outerRadius: Float,
    rotation: Float,
    alpha: Float,
) {
    if (outerRadius <= 4f) return
    val iceStroke = Color(0xFFBAE6FD).copy(alpha = alpha)
    val auroraStroke = Color(0xFF818CF8).copy(alpha = alpha * 0.90f)

    drawCircle(
        color = iceStroke,
        radius = outerRadius * 0.78f,
        center = center,
        style = Stroke(width = 1.0f),
    )

    for (k in 0 until 6) {
        val a = rotation + (k * PI / 3.0).toFloat()
        val tip = Offset(
            center.x + cos(a) * outerRadius,
            center.y + sin(a) * outerRadius,
        )
        drawLine(
            color = iceStroke,
            start = center,
            end = tip,
            strokeWidth = 1.0f,
            cap = StrokeCap.Round,
        )

        val mid = Offset(
            center.x + cos(a) * (outerRadius * 0.56f),
            center.y + sin(a) * (outerRadius * 0.56f),
        )
        val wingLen = outerRadius * 0.28f
        for (side in listOf(-0.52f, 0.52f)) {
            drawLine(
                color = auroraStroke,
                start = mid,
                end = Offset(
                    mid.x + cos(a + side) * wingLen,
                    mid.y + sin(a + side) * wingLen,
                ),
                strokeWidth = 0.9f,
                cap = StrokeCap.Round,
            )
        }
    }
}