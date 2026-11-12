package com.appa.recipeapp.presentation.detail

import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import com.appa.recipeapp.platform.openExternalUrl
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.ui.components.PlaceholderImage
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.appa.recipeapp.ui.components.SkeletonDetailContent
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.ic_timer
import recipeapp.shared.generated.resources.nuka_cap
import com.appa.recipeapp.ui.components.AuthorAvatar
import com.appa.recipeapp.data.model.Comment
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import com.appa.recipeapp.ui.theme.Error
import androidx.compose.material.icons.filled.Close
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> {
            SkeletonDetailContent()
        }
        state.recipe == null -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Background),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.errorMessage ?: "Рецепт не найден",
                    color = TextPrimary,
                    fontSize = 16.sp,
                )
            }
        }
        else -> {
            val recipe = state.recipe!!

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                item {
                    TopPhotoBlock(
                        recipe = recipe,
                        isMyRecipe = state.isMyRecipe,
                        onBackClick = onBackClick,
                        onEditClick = { onEditClick(recipe.id) },
                        onFavoriteClick = { viewModel.toggleFavorite() },
                    )
                }

                item {
                    TitleCard(recipe = recipe)
                }

                item {
                    SectionBlock(title = "Ингредиенты") {
                        IngredientsList(recipe)
                    }
                }

                item {
                    SectionBlock(title = "Приготовление") {
                        StepsList(recipe)
                    }
                }

                item {
                    if (recipe.musicNote.isNotBlank() || recipe.musicTracks.isNotEmpty()) {
                        SectionBlock(title = "Музыка для готовки") {
                            MusicBlock(recipe)
                        }
                    } else {
                        Spacer(Modifier.height(0.dp))
                    }
                }

                if (recipe.notes.isNotBlank()) {
                    item {
                        SectionBlock(title = "Заметки") {
                            Text(
                                text = recipe.notes,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }

                item {
                    SectionBlock(title = "Оценки", showDivider = true) {
                        RatingsBlock(
                            ratings = state.ratings,
                            draftStars = state.draftStars,
                            onStarsChange = viewModel::onStarsChange,
                            onSubmit = viewModel::submitRating,
                            onDeleteMyRating = viewModel::deleteMyRating,
                        )
                    }
                }

                item {
                    SectionBlock(title = "Комментарии", showDivider = false) {
                        CommentsBlock(
                            comments = state.comments,
                            draftComment = state.draftComment,
                            onCommentChange = viewModel::onCommentChange,
                            onSubmit = viewModel::submitComment,
                            currentUserId = state.currentUserID,
                            onDeleteComment = viewModel::deleteComment,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopPhotoBlock(
    recipe: Recipe,
    isMyRecipe: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
    ) {
        val detailPhoto = recipe.photoUrls.firstOrNull()
        if (!detailPhoto.isNullOrBlank()) {
            AsyncImage(
                model = detailPhoto,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            PlaceholderImage(
                seed = recipe.title,
                modifier = Modifier.fillMaxSize(),
            )
        }

        CircleButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            onClick = onBackClick,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isMyRecipe) {
                CircleButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            CircleButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "В избранное",
                    tint = if (recipe.isFavorite) Accent else TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TitleCard(recipe: Recipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-32).dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = recipe.title,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.nuka_cap),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatRating(recipe),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

            MetricDivider()

            AuthorAvatar(
                avatarUrl = recipe.authorAvatarUrl,
                size = 16.dp,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = recipe.authorName,
                color = TextSecondary,
                fontSize = 13.sp,
            )

            MetricDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_timer),
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${recipe.cookingTimeMinutes} мин",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        if (recipe.tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                recipe.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = tag,
                            color = TextSecondary,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        if (recipe.description.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = recipe.description,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    showDivider: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 5.dp, bottom = 20.dp),
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(36.dp))
        content()
        if (showDivider) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerColor.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
private fun IngredientsList(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        recipe.ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = ingredient.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(14.dp)
                        .drawBehind {
                            val dotRadius = 1.dp.toPx()
                            val spacing = 5.dp.toPx()
                            val y = size.height - dotRadius * 2
                            var x = 0f
                            while (x < size.width) {
                                drawCircle(
                                    color = Color(0xFF9E9E9E).copy(alpha = 0.4f),
                                    radius = dotRadius,
                                    center = Offset(x + dotRadius, y),
                                )
                                x += spacing
                            }
                        },
                )
                Text(
                    text = "${ingredient.amount} ${ingredient.unit}".trim(),
                    color = TextSecondary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun StepsList(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        recipe.steps.forEachIndexed { index, step ->
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Background,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (!step.photoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(225.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                AsyncImage(
                                    model = step.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        Text(
                            text = step.text,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }

                if (index < recipe.steps.size - 1) {
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 38.dp)
                            .height(1.dp)
                            .background(DividerColor.copy(alpha = 0.3f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicBlock(recipe: Recipe) {
    Column {
        if (recipe.musicNote.isNotBlank()) {
            Text(
                text = recipe.musicNote,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.height(12.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recipe.musicTracks.forEach { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                        .clickable {
                            if (track.url.isNotBlank()) {
                                openExternalUrl(track.url)
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (track.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "🎵", fontSize = 22.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title.ifBlank { "Трек" },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val source = com.appa.recipeapp.utils.MusicSourceDetector.detect(track.url)
                        Text(
                            text = "${source.displayName} →",
                            color = Accent,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingsBlock(
    ratings: List<Rating>,
    draftStars: Int,
    onStarsChange: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDeleteMyRating: () -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(5) { index ->
                val starIndex = index + 1
                val isSelected = starIndex <= draftStars

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onStarsChange(starIndex) })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.nuka_cap),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = if (isSelected) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    )
                }
            }
        }

        if (draftStars > 0) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Accent.copy(alpha = 0.2f))
                        .clickable { onSubmit() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Оценить", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDeleteMyRating() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Удалить", color = Error, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (ratings.isEmpty()) {
            Text("Пока никто не оценил", color = TextSecondary, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ratings.forEach { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AuthorAvatar(avatarUrl = rating.userAvatarUrl, size = 48.dp)

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = rating.userName,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(rating.stars) {
                                Image(
                                    painter = painterResource(Res.drawable.nuka_cap),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsBlock(
    comments: List<Comment>,
    draftComment: String,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    currentUserId: String,
    onDeleteComment: (String) -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(14.dp),
        ) {
            if (draftComment.isEmpty()) {
                Text("Оставь свой комментарий...", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp)
            }
            BasicTextField(
                value = draftComment,
                onValueChange = onCommentChange,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(10.dp))
                .background(if (draftComment.isNotBlank()) Accent else SurfaceVariant)
                .clickable(enabled = draftComment.isNotBlank()) { onSubmit() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Отправить",
                color = if (draftComment.isNotBlank()) Background else TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(24.dp))

        if (comments.isEmpty()) {
            Text("Пока нет комментариев", color = TextSecondary, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                comments.forEach { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthorAvatar(avatarUrl = comment.userAvatarUrl, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comment.userName.ifBlank { "Гость" }, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(comment.text, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                        }

                        if (comment.userId == currentUserId) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteComment(comment.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    tint = Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(width = 1.dp, height = 14.dp)
            .background(TextSecondary.copy(alpha = 0.4f)),
    )
}

private fun formatRating(recipe: Recipe): String {
    if (recipe.ratingsCount == 0) return "—"
    val rounded = (recipe.averageRating * 10).toInt() / 10.0
    return "$rounded"
}