package com.appa.recipeapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.data.DailyQuote
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuoteCard(
    quote: DailyQuote,
    gusteauImage: DrawableResource,
    modifier: Modifier = Modifier,
) {
    // Бесконечная анимация сдвига для «перелива» градиента
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        // Рамка растягивается по размеру содержимого
        Canvas(
            modifier = Modifier.matchParentSize(),
        ) {
            val strokeWidth = 1.5f
            val corner = 32f

            // 1. ПОСТОЯННАЯ базовая линия — рамка видна всегда
            val baseColor = Color.White.copy(alpha = 0.18f)

            // 2. БЕГУЩИЙ пик — узкая искра, alpha выше базовой
            //    движется от -size.width до 2*size.width, плавно
            //    появляется слева и уходит справа, не «телепортируется»
            val peakPos = (animatedOffset * 4f - 1.5f) * size.width   // от -1.5w до +2.5w   // от -w до 2w
            val peak = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.55f),
                    Color.Transparent,
                ),
                start = Offset(peakPos - 80f, 0f),
                end = Offset(peakPos + 80f, 0f),   // ← горизонтальный градиент
            )

            // Рисуем линии дважды: сначала базовая (постоянная),
            // потом поверх — пик (движется)
            // Верх
            drawLine(baseColor, Offset(corner, 0f), Offset(size.width - corner, 0f), strokeWidth)
            drawLine(peak, Offset(corner, 0f), Offset(size.width - corner, 0f), strokeWidth)
            // Низ
            drawLine(baseColor, Offset(corner, size.height), Offset(size.width - corner, size.height), strokeWidth)
            drawLine(peak, Offset(corner, size.height), Offset(size.width - corner, size.height), strokeWidth)
            // Лево
            drawLine(baseColor, Offset(0f, corner), Offset(0f, size.height - corner), strokeWidth)
            drawLine(peak, Offset(0f, corner), Offset(0f, size.height - corner), strokeWidth)
            // Право
            drawLine(baseColor, Offset(size.width, corner), Offset(size.width, size.height - corner), strokeWidth)
            drawLine(peak, Offset(size.width, corner), Offset(size.width, size.height - corner), strokeWidth)
        }

        // Содержимое — с достаточным внутренним отступом, чтобы не касаться рамки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 28.dp),
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
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "— ${quote.author}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}