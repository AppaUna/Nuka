package com.appa.recipeapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appa.recipeapp.ui.theme.SurfaceVariant

/**
 * Базовый «скелетон» — серый прямоугольник с мягкой пульсацией.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    val infinite = rememberInfiniteTransition(label = "skeleton")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceVariant.copy(alpha = alpha)),
    )
}

/**
 * Скелетон-кружок (для аватаров).
 */
@Composable
fun SkeletonCircle(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "skeletonCircle")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceVariant.copy(alpha = alpha)),
    )
}

/**
 * Скелетон большой карточки рецепта (как RecipeCardLarge) — 220x275.
 */
@Composable
fun SkeletonRecipeCardLarge() {
    SkeletonBox(
        modifier = Modifier
            .width(220.dp)
            .height(275.dp),
        cornerRadius = 18.dp,
    )
}

/**
 * Скелетон средней карточки рецепта (как RecipeCardMedium) — 150x187.
 */
@Composable
fun SkeletonRecipeCardMedium() {
    SkeletonBox(
        modifier = Modifier
            .width(150.dp)
            .height(187.dp),
        cornerRadius = 16.dp,
    )
}

/**
 * Скелетон карточки-строки (как RecipeCardList) — фото 80 + текст.
 */
@Composable
fun SkeletonRecipeCardList() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(
            modifier = Modifier.size(80.dp),
            cornerRadius = 12.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp),
                cornerRadius = 4.dp,
            )
            Spacer(Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp),
                cornerRadius = 4.dp,
            )
        }
    }
}

/**
 * Скелетон заголовка (как ScreenHeader).
 */
@Composable
fun SkeletonScreenHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp),
                cornerRadius = 6.dp,
            )
            Spacer(Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(14.dp),
                cornerRadius = 4.dp,
            )
        }
        Spacer(Modifier.width(12.dp))
        SkeletonCircle(size = 48.dp)
    }
}

/**
 * Скелетон поисковой строки.
 */
@Composable
fun SkeletonSearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            cornerRadius = 24.dp,
        )
    }
}

/**
 * Скелетон чипов-фильтров.
 */
@Composable
fun SkeletonTagRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        repeat(4) {
            SkeletonBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp),
                cornerRadius = 20.dp,
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

/**
 * Скелетон заголовка секции (как SectionHeader).
 */
@Composable
fun SkeletonSectionHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp),
            cornerRadius = 4.dp,
        )
    }
}