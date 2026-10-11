package com.appa.recipeapp.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.platform.saveImageToFile
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import com.github.skydoves.crayfish.decode.CropSource
import com.github.skydoves.crayfish.ui.AspectRatio
import com.github.skydoves.crayfish.ui.CropImage
import com.github.skydoves.crayfish.ui.Cropper
import com.github.skydoves.crayfish.ui.rememberCropState
import kotlinx.coroutines.launch

@Composable
fun CropScreen(
    photoUri: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberCropState(
        source = CropSource.FilePath(photoUri),
        initialAspectRatio = AspectRatio.Fixed(4f / 5f),   // ← 4:5
    )
    val scope = rememberCoroutineScope()

    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Шапка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Отмена",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Text(
                text = "Обрезка",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (isSaving) "..." else "Готово",
                color = if (isSaving) TextSecondary else Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isSaving) {
                        scope.launch {
                            isSaving = true
                            when (val result = state.cropToImage()) {
                                is CropImage.Success -> {
                                    val savedPath = saveImageToFile(result.image)
                                    if (savedPath.isNotBlank()) {
                                        onConfirm(savedPath)
                                    } else {
                                        isSaving = false
                                    }
                                }
                                is CropImage.Cancelled -> {
                                    isSaving = false
                                }
                                is CropImage.Failure -> {
                                    isSaving = false
                                }
                            }
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        // Кроп
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Cropper(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}