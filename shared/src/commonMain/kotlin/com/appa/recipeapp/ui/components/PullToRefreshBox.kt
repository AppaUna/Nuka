package com.appa.recipeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.appa.recipeapp.ui.theme.Accent

/**
 * Кастомный pull-to-refresh, совместимый с Compose Multiplatform.
 *
 * Маленький аккуратный индикатор выезжает сверху по мере натяжения.
 */
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 60.dp.toPx() }
    val maxPullPx = with(density) { 100.dp.toPx() }
    val indicatorSizePx = with(density) { 36.dp.toPx() }

    var pullOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0 && pullOffsetPx > 0f) {
                    val consumed = available.y.coerceAtMost(pullOffsetPx)
                    pullOffsetPx = (pullOffsetPx - consumed).coerceAtLeast(0f)
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0) {
                    pullOffsetPx = (pullOffsetPx + available.y * 0.7f).coerceIn(0f, maxPullPx)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullOffsetPx >= thresholdPx && !isRefreshing) {
                    onRefresh()
                }
                pullOffsetPx = 0f
                return Velocity.Zero
            }
        }
    }

    val pullProgress = (pullOffsetPx / thresholdPx).coerceIn(0f, 1f)

    // Позиция индикатора:
    // - pull: от -indicatorSize (скрыт за верхом) до pullOffset
    // - refreshing: фикс. отступ сверху 16dp
    val targetOffsetPx = if (isRefreshing) {
        with(density) { 16.dp.toPx() }
    } else {
        (pullOffsetPx - indicatorSizePx).coerceAtLeast(-indicatorSizePx)
    }

    val animatedOffset by animateFloatAsState(
        targetValue = targetOffsetPx,
        animationSpec = tween(durationMillis = 220),
        label = "pullOffset",
    )

    val alpha = if (isRefreshing) 1f else (pullProgress * 1.5f).coerceIn(0f, 1f)
    val rotation = pullProgress * 180f

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
    ) {
        // ─── Индикатор ───
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = with(density) { animatedOffset.toDp() })
                .alpha(alpha),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = Accent.copy(alpha = 0.6f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = Accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(18.dp)
                        .then(
                            if (isRefreshing) Modifier
                            else Modifier.rotate(rotation)
                        ),
                )
            }
        }

        // ─── Контент ───
        content()
    }
}