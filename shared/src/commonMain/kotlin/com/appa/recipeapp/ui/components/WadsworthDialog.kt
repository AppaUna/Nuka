package com.appa.recipeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.data.WadsworthRepository
import com.appa.recipeapp.platform.AudioPlayer
import com.appa.recipeapp.platform.createSettings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.share_tech_mono
import recipeapp.shared.generated.resources.wadsworth

// Приглушённые цвета — атмосфера пустоши
private val FalloutGreen = Color(0xFF00C853)
private val FalloutGreenDim = Color(0xFF4CAF50)
private val FalloutGrey = Color(0xFF6B6B6B)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun WadsworthDialog(
    jokeText: String,
    jokeAudioFile: String,
    onClose: () -> Unit,
) {
    val settings = remember { createSettings() }
    val currentSlot = remember { WadsworthRepository.getCurrentSlotId() }
    val audioPlayer = remember { AudioPlayer() }
    val scope = rememberCoroutineScope()

    var showJoke by remember { mutableStateOf(false) }
    var jokeSeenInCurrentSlot by remember {
        mutableStateOf(settings.getIntOrNull("joke_seen_slot") == currentSlot)
    }

    // Общая функция для воспроизведения — используется в обоих случаях
    val playJokeAudio: () -> Unit = {
        scope.launch {
            try {
                val bytes = Res.readBytes("files/jokes/$jokeAudioFile")
                audioPlayer.play(bytes)
            } catch (e: Exception) {
                // Без звука — если файл не найден
            }
        }
    }

    // Останавливаем звук при закрытии окна
    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    // Затемнение фона
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        // Окно терминала
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0A0A))
                .border(
                    width = 1.dp,
                    color = FalloutGreen.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(enabled = false) { },
        ) {
            // Тонкие сканлайны (эффект CRT)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = List(40) { index ->
                                if (index % 2 == 0) FalloutGreen.copy(alpha = 0.03f)
                                else Color.Transparent
                            },
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Шапка терминала
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        TerminalText(
                            text = "ROBC0 TERMLINK PROTOCOL",
                            fontSize = 11.sp,
                            color = FalloutGreen,
                            weight = FontWeight.Bold,
                        )
                        TerminalText(
                            text = "WELCOME, OVERSEER",
                            fontSize = 10.sp,
                            color = FalloutGreenDim,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.dp, FalloutGreen.copy(alpha = 0.6f), CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        TerminalText(
                            text = "X",
                            fontSize = 12.sp,
                            color = FalloutGreen,
                            weight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(FalloutGreen.copy(alpha = 0.3f)),
                )
                Spacer(Modifier.height(16.dp))

                // PNG Уодсворта + подпись справа
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.wadsworth),
                        contentDescription = "Wadsworth",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(140.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    TerminalText(
                        text = "— Wadsworth",
                        fontSize = 12.sp,
                        color = FalloutGreenDim,
                        fontStyle = FontStyle.Italic,
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (!showJoke) {
                    // Варианты ответов
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DialogueOption(
                            text = "I'd like a haircut, please.",
                            color = FalloutGrey,
                            onClick = { /* TODO */ },
                        )
                        DialogueOption(
                            text = "Can you explain how the My First Laboratory works?",
                            color = FalloutGrey,
                            onClick = { /* TODO */ },
                        )

                        if (!jokeSeenInCurrentSlot) {
                            // Первый раз — зелёная кнопка
                            DialogueOption(
                                text = "Tell me a joke.",
                                color = FalloutGreen,
                                onClick = {
                                    showJoke = true
                                    jokeSeenInCurrentSlot = true
                                    settings.putInt("joke_seen_slot", currentSlot)
                                    playJokeAudio()
                                },
                            )
                        } else {
                            // Уже смотрел — серая, но звук всё равно играет
                            DialogueOption(
                                text = "Tell me a joke.",
                                color = FalloutGrey,
                                onClick = {
                                    showJoke = true
                                    playJokeAudio()
                                },
                            )
                        }
                    }
                } else {
                    // Текст анекдота
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TerminalText(
                            text = "> $jokeText",
                            fontSize = 13.sp,
                            color = FalloutGreen,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogueOption(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TerminalText(
            text = "> $text",
            fontSize = 13.sp,
            color = color,
        )
    }
}

@Composable
private fun TerminalText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = weight,
        fontStyle = fontStyle,
        lineHeight = lineHeight,
        fontFamily = FontFamily(
            Font(Res.font.share_tech_mono, FontWeight.Normal),
        ),
    )
}