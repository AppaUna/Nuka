package com.appa.recipeapp.presentation.profile

import com.appa.recipeapp.ui.components.ScreenHeader
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.ui.components.RecipeImage
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.profile_decor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditRecipe: (String) -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
) {
    // ─── ДИАГНОСТИКА ───
    val compositionTime = remember { Clock.System.now().toEpochMilliseconds() }
    LaunchedEffect(Unit) {
        println("DEBUG ProfileScreen: composition+firstFrame ${Clock.System.now().toEpochMilliseconds() - compositionTime} мс, vmHash=${viewModel.hashCode()}")
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshIfStale()
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.profile_decor),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alpha = 0.12f,
            colorFilter = ColorFilter.colorMatrix(
                ColorMatrix().apply { setToSaturation(0f) }
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(280.dp)
                .offset(x = 60.dp, y = 60.dp),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                item {
                    ScreenHeader(title = "Профиль")
                }

                item {
                    UserBlock(
                        login = state.login,
                        displayName = state.displayName,
                        nameColor = parseHexColor(state.selectedNameColor),
                    )
                }

                item {
                    StatsRow(
                        recipes = state.recipesCount,
                        ratings = state.ratingsCount,
                        favorites = state.favoritesCount,
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    ExpandableHeader(
                        title = "Мои рецепты",
                        isExpanded = state.isMyRecipesExpanded,
                        onClick = viewModel::toggleMyRecipes,
                    )
                }
                if (state.isMyRecipesExpanded) {
                    if (state.myRecipes.isEmpty()) {
                        item {
                            Text(
                                text = "Нет рецептов",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    } else {
                        items(state.myRecipes, key = { it.id }) { recipe ->
                            MyRecipeRow(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onEdit = { onEditRecipe(recipe.id) },
                                onDelete = { viewModel.onDeleteRecipe(recipe.id) },
                            )
                        }
                    }
                }
                item { ThinDivider() }

                item {
                    ExpandableHeader(
                        title = "Персонализация",
                        isExpanded = state.isPersonalizationExpanded,
                        onClick = viewModel::togglePersonalization,
                    )
                }
                if (state.isPersonalizationExpanded) {
                    item {
                        EditableNameRow(
                            currentName = state.displayName,
                            isEditing = state.isEditingName,
                            draft = state.nameDraft,
                            onStartEdit = viewModel::startEditingName,
                            onDraftChange = viewModel::onNameDraftChange,
                            onSave = viewModel::saveName,
                            onCancel = viewModel::cancelEditingName,
                        )
                    }
                    item {
                        ColorPickerRow(
                            selectedColor = state.selectedNameColor,
                            onColorSelected = viewModel::selectNameColor,
                        )
                    }
                    item {
                        PersonalizationRow(label = "Тема", value = "Тёмная")
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Accent)
                                .clickable { viewModel.applyPersonalization() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Применить",
                                color = Background,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                item { ThinDivider() }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "Выйти из аккаунта",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.logout() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// КОМПОНЕНТЫ
// ═══════════════════════════════════════════

@Composable
private fun UserBlock(
    login: String,
    displayName: String,
    nameColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayName.take(1).uppercase(),
                color = Background,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = login,   // ← БЕЗ @
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = displayName,
            color = nameColor,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun StatsRow(recipes: Int, ratings: Int, favorites: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(label = "Рецептов", value = recipes)
        StatDivider()
        StatItem(label = "Оценок", value = ratings)
        StatDivider()
        StatItem(label = "Избранных", value = favorites)
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 22.dp)
            .background(DividerColor),
    )
}

@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(DividerColor.copy(alpha = 0.5f)),
    )
}

@Composable
private fun ExpandableHeader(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation),
        )
    }
}

@Composable
private fun MyRecipeRow(
    recipe: Recipe,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            photoUri = recipe.photoUrls.firstOrNull(),
            seed = recipe.title,
            cornerRadius = 10.dp,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = recipe.title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onEdit),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✎", color = Accent, fontSize = 16.sp)
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", color = TextSecondary, fontSize = 16.sp)
        }
    }
}

@Composable
private fun EditableNameRow(
    currentName: String,
    isEditing: Boolean,
    draft: String,
    onStartEdit: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Отображаемое имя",
            color = TextPrimary,
            fontSize = 14.sp,
        )
        if (isEditing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    BasicTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        singleLine = true,
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(Accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "✓",
                    color = Accent,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable(onClick = onSave),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "✕",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable(onClick = onCancel),
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentName,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "✎",
                    color = Accent,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onStartEdit),
                )
            }
        }
    }
}

@Composable
private fun PersonalizationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp)
        Text(
            text = value,
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ColorPickerRow(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
) {
    val palette = listOf(
        "#F5A623",
        "#E53935",
        "#FDD835",
        "#43A047",
        "#1E88E5",
        "#8E24AA",
        "#EC407A",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Цвет имени",
            color = TextPrimary,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            palette.forEach { hex ->
                val isSelected = hex == selectedColor
                val color = parseHexColor(hex)

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = TextPrimary,
                                    shape = CircleShape,
                                )
                            } else Modifier
                        )
                        .clickable { onColorSelected(hex) },
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "(несомненно самая важная функция приложения)",
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

private fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    val r = cleaned.substring(0, 2).toLong(16)
    val g = cleaned.substring(2, 4).toLong(16)
    val b = cleaned.substring(4, 6).toLong(16)
    return Color(0xFF000000 or (r shl 16) or (g shl 8) or b)
}