package com.appa.recipeapp.navigation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.ic_add
import recipeapp.shared.generated.resources.ic_favorite
import recipeapp.shared.generated.resources.ic_home
import recipeapp.shared.generated.resources.ic_person
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (Routes) -> Unit,
    onCreateClick: () -> Unit,
    isNewYear: Boolean = false,
) {
    val bg = MaterialTheme.colorScheme.background

    // Плавный прозрачный градиент, чтобы сугроб экрана и контент мягко уходили под панель
    val gradient = Brush.verticalGradient(
        colors = listOf(
            bg.copy(alpha = 0.0f),
            bg.copy(alpha = 0.14f),
            bg.copy(alpha = 0.34f),
            bg.copy(alpha = 0.56f),
            bg.copy(alpha = 0.78f),
            bg.copy(alpha = 0.92f),
            bg.copy(alpha = 1.0f),
        ),
    )

    val flameTransition = rememberInfiniteTransition(label = "icon_flames")
    val flowTime by flameTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flame_flow_time",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BarItem(
                    label = "Главная",
                    selected = currentRoute == Routes.Home.route,
                    showFlame = isNewYear,
                    flowTime = flowTime,
                    onClick = { onNavigate(Routes.Home) },
                    iconRes = Res.drawable.ic_home,
                )

                BarItem(
                    label = "Избранное",
                    selected = currentRoute == Routes.Favorites.route,
                    showFlame = isNewYear,
                    flowTime = flowTime,
                    onClick = { onNavigate(Routes.Favorites) },
                    iconRes = Res.drawable.ic_favorite,
                )

                BarItem(
                    label = "Добавить",
                    selected = false,
                    showFlame = false,
                    showStarHalo = isNewYear,
                    flowTime = flowTime,
                    onClick = onCreateClick,
                    iconRes = Res.drawable.ic_add,
                    forceAccent = true,
                )

                BarItem(
                    label = "Профиль",
                    selected = currentRoute == Routes.Profile.route,
                    showFlame = isNewYear,
                    flowTime = flowTime,
                    onClick = { onNavigate(Routes.Profile) },
                    iconRes = Res.drawable.ic_person,
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    label: String,
    selected: Boolean,
    showFlame: Boolean,
    flowTime: Float,
    onClick: () -> Unit,
    iconRes: DrawableResource,
    forceAccent: Boolean = false,
    showStarHalo: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val iconColor = if (forceAccent || selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val textColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val iconSize = if (forceAccent) 28.dp else 24.dp
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (selected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bar_item_spring",
    )

    val flameStrength by animateFloatAsState(
        targetValue = if (showFlame && selected) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "tab_flame_ignite",
    )

    Box(
        modifier = Modifier
            .size(width = 68.dp, height = 48.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showStarHalo) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.38f
                val rot = flowTime * 0.8f
                val outerR = 19.dp.toPx()
                val innerR = 7.dp.toPx()
                for (k in 0 until 4) {
                    val a = rot + k * (PI.toFloat() / 4f)
                    val len = if (k % 2 == 0) outerR else outerR * 0.65f
                    drawLine(
                        color = Color(0xFF5CE1E6).copy(alpha = 0.28f),
                        start = Offset(cx + cos(a) * innerR, cy + sin(a) * innerR),
                        end = Offset(cx + cos(a) * len, cy + sin(a) * len),
                        strokeWidth = 1.1f,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color(0xFF5CE1E6).copy(alpha = 0.28f),
                        start = Offset(cx - cos(a) * innerR, cy - sin(a) * innerR),
                        end = Offset(cx - cos(a) * len, cy - sin(a) * len),
                        strokeWidth = 1.1f,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        if (flameStrength > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val t = flowTime * 3.1f
                val sway1 = (sin(t * 1.15f) * 0.65f + cos(t * 0.71f) * 0.35f) * w * 0.06f
                val sway2 = (sin(t * 1.43f + 1.2f) * 0.6f + cos(t * 0.89f) * 0.4f) * w * 0.05f
                val tipLift1 = 0.08f + 0.04f * (sin(t * 1.7f) * 0.6f + cos(t * 1.1f) * 0.4f)
                val tipLift2 = 0.20f + 0.04f * (sin(t * 2.1f + 0.8f) * 0.6f + cos(t * 1.3f) * 0.4f)

                // Внешний ледяной язык пламени (Северное сияние)
                val outerTongue = Path().apply {
                    moveTo(w * 0.28f, h * 0.78f)
                    cubicTo(
                        w * 0.22f, h * 0.48f,
                        w * 0.38f + sway1, h * 0.22f,
                        w * 0.50f + sway1, h * tipLift1,
                    )
                    cubicTo(
                        w * 0.62f + sway1, h * 0.25f,
                        w * 0.78f, h * 0.48f,
                        w * 0.72f, h * 0.78f,
                    )
                    close()
                }
                drawPath(
                    path = outerTongue,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5CE1E6).copy(alpha = 0.52f * flameStrength),
                            Color(0xFF38D399).copy(alpha = 0.24f * flameStrength),
                            Color.Transparent,
                        ),
                        startY = h * tipLift1,
                        endY = h * 0.80f,
                    ),
                )

                // Внутреннее тёплое ядро кулинарного очага
                val innerCore = Path().apply {
                    moveTo(w * 0.40f, h * 0.78f)
                    cubicTo(
                        w * 0.35f, h * 0.54f,
                        w * 0.46f + sway2, h * 0.34f,
                        w * 0.54f + sway2, h * tipLift2,
                    )
                    cubicTo(
                        w * 0.64f + sway2, h * 0.40f,
                        w * 0.68f, h * 0.60f,
                        w * 0.62f, h * 0.78f,
                    )
                    close()
                }
                drawPath(
                    path = innerCore,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFDE68A).copy(alpha = 0.48f * flameStrength),
                            Color(0xFFFBBF24).copy(alpha = 0.20f * flameStrength),
                            Color.Transparent,
                        ),
                        startY = h * tipLift2,
                        endY = h * 0.80f,
                    ),
                )

                // Поднимающиеся микро-искры
                for (e in 0 until 3) {
                    val cycle = ((flowTime * 0.9f + e * 0.33f) % 1f)
                    val ex = w * (0.44f + e * 0.06f) + sin(t * 2f + e) * 4f
                    val ey = h * (0.62f - cycle * 0.54f)
                    val eAlpha = (1f - cycle) * 0.65f * flameStrength
                    drawCircle(
                        color = if (e == 1) Color(0xFFFDE68A).copy(alpha = eAlpha)
                        else Color(0xFF5CE1E6).copy(alpha = eAlpha),
                        radius = 1.6f,
                        center = Offset(ex, ey),
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
            )
        }
    }
}