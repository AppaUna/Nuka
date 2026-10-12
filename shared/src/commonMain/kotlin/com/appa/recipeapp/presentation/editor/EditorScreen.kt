package com.appa.recipeapp.presentation.editor

import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.bg_texture
import recipeapp.shared.generated.resources.editor_decor
import coil3.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.platform.rememberPhotoPicker
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.exit_dialog
import recipeapp.shared.generated.resources.bg_texture
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vinceglb.filekit.PlatformFile

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Пикер фото
    val scope = rememberCoroutineScope()

    val photoPicker = rememberPhotoPicker(
        onPhotoPicked = { file ->
            viewModel.onCropStart(file)
        }
    )

    val stepPhotoPicker = rememberPhotoPicker(
        onPhotoPicked = { file ->
            viewModel.onCropStart(file, viewModel.getPendingStepId())
        }
    )


    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    // Экран кропа
    if (state.isCropping && state.croppingFile != null) {
        CropScreen(
            photoFile = state.croppingFile!!,
            onConfirm = { bytes -> viewModel.onCropConfirm(bytes) },
            onCancel = { viewModel.onCropCancel() },
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Фоновая текстура приложения
        Image(
            painter = painterResource(Res.drawable.bg_texture),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.15f,
            modifier = Modifier.matchParentSize(),
        )

        // 2. PNG-декор справа снизу, ПОД контентом
        Image(
            painter = painterResource(Res.drawable.editor_decor),
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

        // 3. Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            EditorTopBar(
                title = state.screenTitle,
                canSave = state.isValid && !state.isSaving,
                saveStatusText = state.saveStatusText,
                isSaving = state.isSaving,
                onBack = {
                    if (state.hasChanges) {
                        viewModel.onBackPressed()
                    } else {
                        onBack()
                    }
                },
                onSave = viewModel::save,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
            ) {
                PhotoPickerBlock(
                    photoUri = state.photoUri,
                    onClick = { photoPicker.launch() },
                )

                EditorField(
                    label = "Название",
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = "Введи название рецепта...",
                    singleLine = true,
                )

                TagsSection(
                    allTags = state.allTags,
                    selectedTags = state.selectedTags,
                    newTagDraft = state.newTagDraft,
                    onTagClick = viewModel::toggleTag,
                    onNewTagChange = viewModel::onNewTagChange,
                    onAddNewTag = viewModel::addNewTag,
                )

                EditorField(
                    label = "Описание",
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "Расскажи о рецепте...",
                    singleLine = false,
                    minHeight = 100.dp,
                )

                EditorField(
                    label = "Время готовки (мин)",
                    value = state.cookingTime,
                    onValueChange = viewModel::onCookingTimeChange,
                    placeholder = "30",
                    singleLine = true,
                )

                IngredientsSection(
                    ingredients = state.ingredients,
                    onAdd = viewModel::addIngredient,
                    onRemove = viewModel::removeIngredient,
                    onNameChange = viewModel::updateIngredientName,
                    onQuantityChange = viewModel::updateIngredientQuantity,
                )

                StepsSection(
                    steps = state.steps,
                    onAdd = viewModel::addStep,
                    onRemove = viewModel::removeStep,
                    onTextChange = viewModel::updateStep,
                    onPhotoClick = { stepId ->
                        viewModel.onStepPhotoClicked(stepId)
                        stepPhotoPicker.launch()
                    },
                )

                MusicSection(
                    note = state.musicNote,
                    tracks = state.musicTracks,
                    onNoteChange = viewModel::onMusicNoteChange,
                    onAddTrack = viewModel::addMusicTrack,
                    onRemoveTrack = viewModel::removeMusicTrack,
                    onTrackTitleChange = viewModel::updateMusicTrackTitle,
                    onTrackUrlChange = viewModel::updateMusicTrackUrl,
                )
            }
        }

        if (state.showExitDialog) {
            ExitConfirmDialog(
                onCancel = { viewModel.dismissExitDialog() },
                onConfirm = {
                    viewModel.dismissExitDialog()
                    onBack()
                },
            )
        }
    }
}

// ═══════════════════════════════════════════
// ШАПКА
// ═══════════════════════════════════════════

@Composable
private fun EditorTopBar(
    title: String,
    canSave: Boolean,
    saveStatusText: String,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 0.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = saveStatusText,
                color = if (canSave && !isSaving) Accent
                else if (isSaving) Accent
                else TextSecondary.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = canSave && !isSaving, onClick = onSave)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════
// ФОТО
// ═══════════════════════════════════════════

@Composable
private fun PhotoPickerBlock(
    photoUri: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Surface.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = TextSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (photoUri == null) {
                // Пустой блок с плюсом
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+",
                        color = Accent,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Добавить фото",
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                }
            } else {
                // Реальное фото через Coil
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// УНИВЕРСАЛЬНОЕ ПОЛЕ
// ═══════════════════════════════════════════

@Composable
private fun EditorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface.copy(alpha = 0.6f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp,
                ),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!singleLine) Modifier.height(minHeight)
                        else Modifier
                    ),
            )
        }
    }
}

// ═══════════════════════════════════════════
// ТЕГИ
// ═══════════════════════════════════════════

@Composable
private fun TagsSection(
    allTags: List<String>,
    selectedTags: Set<String>,
    newTagDraft: String,
    onTagClick: (String) -> Unit,
    onNewTagChange: (String) -> Unit,
    onAddNewTag: () -> Unit,
) {
    // Все доступные теги: популярные (не входящие в обязательные) + динамические из БД
    val popularNotRequired = POPULAR_TAGS.filter { it !in REQUIRED_TAGS }
    val dynamicTags = allTags
        .map { it.lowercase() }
        .filter { it !in REQUIRED_TAGS && it !in popularNotRequired }
        .distinct()
        .sorted()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        // ─── Обязательные теги ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Приём пищи",
                color = TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "(обязательно выбери хотя бы один)",
                color = Accent.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(REQUIRED_TAGS, key = { it }) { tag ->
                TagChipEditor(
                    label = tag,
                    isSelected = tag in selectedTags,
                    onClick = { onTagClick(tag) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Остальные теги ───
        Text(
            text = "Дополнительные теги",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        val extraTags = popularNotRequired + dynamicTags
        if (extraTags.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val ordered = extraTags.sortedByDescending { it in selectedTags }
                items(ordered, key = { it }) { tag ->
                    TagChipEditor(
                        label = tag,
                        isSelected = tag in selectedTags,
                        onClick = { onTagClick(tag) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ─── Новый тег ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (newTagDraft.isEmpty()) {
                    Text(
                        text = "Новый тег...",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                    )
                }
                BasicTextField(
                    value = newTagDraft,
                    onValueChange = onNewTagChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Accent)
                    .clickable(onClick = onAddNewTag),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "+", color = Background, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun TagChipEditor(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Accent else SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            color = if (isSelected) Background else TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// ═══════════════════════════════════════════
// ИНГРЕДИЕНТЫ
// ═══════════════════════════════════════════

@Composable
private fun IngredientsSection(
    ingredients: List<IngredientDraft>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    onNameChange: (Long, String) -> Unit,
    onQuantityChange: (Long, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Text(
            text = "Ингредиенты",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        ingredients.forEach { ingredient ->
            IngredientRow(
                ingredient = ingredient,
                onNameChange = { text -> onNameChange(ingredient.id, text) },
                onQuantityChange = { text -> onQuantityChange(ingredient.id, text) },
                onRemove = { onRemove(ingredient.id) },
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAdd)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "+", color = Accent, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Добавить ингредиент",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: IngredientDraft,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (ingredient.name.isEmpty()) {
                Text(
                    text = "Название",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                )
            }
            BasicTextField(
                value = ingredient.name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(90.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (ingredient.quantity.isEmpty()) {
                Text(
                    text = "300 г",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                )
            }
            BasicTextField(
                value = ingredient.quantity,
                onValueChange = onQuantityChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════
// ШАГИ ПРИГОТОВЛЕНИЯ
// ═══════════════════════════════════════════

@Composable
private fun StepsSection(
    steps: List<StepDraft>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    onTextChange: (Long, String) -> Unit,
    onPhotoClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Text(
            text = "Приготовление",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        steps.forEachIndexed { index, step ->
            StepRow(
                number = index + 1,
                step = step,
                onTextChange = { text -> onTextChange(step.id, text) },
                onRemove = { onRemove(step.id) },
                onPhotoClick = { onPhotoClick(step.id) },
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAdd)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "+", color = Accent, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Добавить шаг",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun StepRow(
    number: Int,
    step: StepDraft,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    onPhotoClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                color = Background,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (step.text.isEmpty()) {
                    Text(
                        text = "Опиши шаг...",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                    )
                }
                BasicTextField(
                    value = step.text,
                    onValueChange = onTextChange,
                    singleLine = false,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                )
            }

            Spacer(Modifier.height(6.dp))

            StepPhotoBlock(
                photoUri = step.photoUri,
                onClick = onPhotoClick,
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun StepPhotoBlock(
    photoUri: String?,
    onClick: () -> Unit,
) {
    if (photoUri == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = TextSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "+", color = Accent, fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Добавить фото к шагу",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ═══════════════════════════════════════════
// МУЗЫКА
// ═══════════════════════════════════════════

@Composable
private fun MusicSection(
    note: String,
    tracks: List<MusicTrackDraft>,
    onNoteChange: (String) -> Unit,
    onAddTrack: () -> Unit,
    onRemoveTrack: (Long) -> Unit,
    onTrackTitleChange: (Long, String) -> Unit,
    onTrackUrlChange: (Long, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Text(
            text = "Музыка",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        MusicField(
            placeholder = "Посоветуй музыку для готовки...",
            value = note,
            onValueChange = onNoteChange,
            singleLine = false,
            minHeight = 80.dp,
        )

        Spacer(Modifier.height(12.dp))

        tracks.forEach { track ->
            MusicTrackRow(
                track = track,
                onTitleChange = { text -> onTrackTitleChange(track.id, text) },
                onUrlChange = { text -> onTrackUrlChange(track.id, text) },
                onRemove = { onRemoveTrack(track.id) },
            )
            Spacer(Modifier.height(6.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAddTrack)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "+", color = Accent, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Добавить трек",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MusicField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 13.sp,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
            cursorBrush = SolidColor(Accent),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!singleLine) Modifier.height(minHeight)
                    else Modifier
                ),
        )
    }
}

@Composable
private fun MusicTrackRow(
    track: MusicTrackDraft,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (track.title.isEmpty()) {
                    Text(
                        text = "Название трека",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                    )
                }
                BasicTextField(
                    value = track.title,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (track.url.isEmpty()) {
                    Text(
                        text = "spotify.com/... или music.yandex.ru/...",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                    )
                }
                BasicTextField(
                    value = track.url,
                    onValueChange = onUrlChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════
// ДИАЛОГ ОТМЕНЫ
// ═══════════════════════════════════════════

@Composable
private fun ExitConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Background)
                .clickable(enabled = false) { },
        ) {
            Image(
                painter = painterResource(Res.drawable.bg_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.15f,
                modifier = Modifier.matchParentSize(),
            )

            Image(
                painter = painterResource(Res.drawable.exit_dialog),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = 0.15f,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(110.dp)
                    .offset(x = 10.dp, y = 20.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Выйти без сохранения?",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedDialogButton(
                        label = "Отмена",
                        onClick = onCancel,
                    )
                    OutlinedDialogButton(
                        label = "Выйти",
                        onClick = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlinedDialogButton(
    label: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderBrush = if (isPressed) {
        Brush.linearGradient(listOf(Accent, Accent, Accent))
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.25f),
                Color.Gray.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.25f),
            ),
        )
    }

    val contentColor = if (isPressed) Accent else TextPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .border(
                width = 0.7.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 24.dp),
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}