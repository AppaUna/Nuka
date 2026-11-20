package com.appa.recipeapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.ui.theme.NewYearTheme
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.ic_favorite
import recipeapp.shared.generated.resources.ic_timer
import recipeapp.shared.generated.resources.nuka_cap

@Composable
fun RecipeCardList(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val isNewYear = accent == NewYearTheme.colors.accent

    val favInteraction = remember { MutableInteractionSource() }
    val isFavPressed by favInteraction.collectIsPressedAsState()

    val iconTint by animateColorAsState(
        targetValue = when {
            recipe.isFavorite -> accent
            isNewYear -> Color(0xFFBAE6FD).copy(alpha = 0.52f)
            else -> TextSecondary
        },
        animationSpec = tween(durationMillis = 220),
        label = "favorite_tint",
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isFavPressed) 0.84f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 550f),
        label = "favorite_scale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .frostHorizontalCard(enabled = isNewYear, cornerRadius = 14.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            photoUri = recipe.photoUrls.firstOrNull(),
            seed = recipe.title,
            cornerRadius = 12.dp,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = recipe.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (recipe.description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = recipe.description,
                    color = TextSecondary.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.nuka_cap),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = formatRating(recipe),
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            if (isNewYear) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = favInteraction,
                            indication = null,
                            onClick = onFavoriteClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_favorite),
                        contentDescription = "В избранное",
                        tint = if (isFavPressed) Color(0xFFE0F2FE) else iconTint,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariant)
                        .clickable(onClick = onFavoriteClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_favorite),
                        contentDescription = "В избранное",
                        tint = if (recipe.isFavorite) accent else TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_timer),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(11.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "${recipe.cookingTimeMinutes} мин",
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun formatRating(recipe: Recipe): String {
    if (recipe.ratingsCount == 0) return "—"
    val rounded = (recipe.averageRating * 10).toInt() / 10.0
    return "$rounded (${recipe.ratingsCount})"
}