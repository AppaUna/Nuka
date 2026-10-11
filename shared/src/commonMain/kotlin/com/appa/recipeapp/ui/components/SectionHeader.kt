package com.appa.recipeapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.TextPrimary

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        if (onSeeAllClick != null) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    val direction = if (targetState) -1 else 1
                    (slideInHorizontally(
                        animationSpec = tween(durationMillis = 220),
                        initialOffsetX = { it * direction },
                    ) + fadeIn(animationSpec = tween(durationMillis = 220)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(durationMillis = 220),
                                targetOffsetX = { -it * direction },
                            ) + fadeOut(animationSpec = tween(durationMillis = 220))
                        )
                },
                label = "seeAllButton",
            ) { expanded ->
                Text(
                    text = if (expanded) "← Свернуть" else "Все →",
                    color = Accent,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable(onClick = onSeeAllClick)
                        .padding(4.dp),
                )
            }
        }
    }
}