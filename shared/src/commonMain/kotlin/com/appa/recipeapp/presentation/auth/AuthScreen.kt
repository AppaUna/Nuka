package com.appa.recipeapp.presentation.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.logo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // FocusManager — для снятия фокуса при тапе по пустому месту и смене режима
    val focusManager = LocalFocusManager.current

    // ─── Анимация: свечение блуждает по экрану ───
    val infinite = rememberInfiniteTransition(label = "authGlow")

    // Полный цикл обхода — 40 секунд (медленно)
    val roamProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
        ),
        label = "roamProgress",
    )

    val t = roamProgress * 2f * PI.toFloat()

    // ТОЛЬКО целые частоты — иначе телепорт в конце цикла
    val normX = (sin(t * 1f) * 0.35f + sin(t * 2f) * 0.15f + 0.5f)
        .coerceIn(0f, 1f)
    val normY = (cos(t * 1f) * 0.35f + cos(t * 3f) * 0.15f + 0.5f)
        .coerceIn(0f, 1f)

    // Тусклое мерцание — ярче/тусклее
    val flickerAlpha by infinite.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flickerAlpha",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
    ) {
        // ─── Размеры экрана в пикселях — кросс-платформенно ───
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val centerX = normX * screenWidthPx
        val centerY = normY * screenHeightPx

        // ─── ФОН: глобальный bg_texture из App.kt, без затемнения ───

        // ─── Свечение: маленький тусклый огонёк, блуждает по экрану ───
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Accent.copy(alpha = flickerAlpha),
                            Accent.copy(alpha = flickerAlpha * 0.35f),
                            Color.Transparent,
                        ),
                        center = Offset(x = centerX, y = centerY),
                        radius = 180f,
                    )
                ),
        )

        // ─── КОНТЕНТ ───
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(100.dp),
                )

                Spacer(Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(top = 230.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFFFFFFF),
                                            Color(0xFFFFE0B8),
                                            Color(0xFFFFC078),
                                            Accent,
                                            Accent,
                                        ),
                                    )
                                )
                            ) {
                                append("NukApp")
                            }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ToggleText(
                            text = "Вход",
                            selected = !state.isRegisterMode,
                            onClick = {
                                if (state.isRegisterMode) {
                                    focusManager.clearFocus()
                                    viewModel.toggleMode()
                                }
                            },
                        )
                        Text(
                            text = "  ·  ",
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                        ToggleText(
                            text = "Регистрация",
                            selected = state.isRegisterMode,
                            onClick = {
                                if (!state.isRegisterMode) {
                                    focusManager.clearFocus()
                                    viewModel.toggleMode()
                                }
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            UnderlineField(
                value = state.login,
                onValueChange = viewModel::onLoginChange,
                label = "Логин",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(24.dp))

            if (state.isRegisterMode) {
                UnderlineField(
                    value = state.displayName,
                    onValueChange = viewModel::onDisplayNameChange,
                    label = "Имя",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                )
                Spacer(Modifier.height(24.dp))
            }

            UnderlineField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Пароль",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.errorMessage!!,
                    color = Color(0xFFE05A5A),
                    fontSize = 12.sp,
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (state.isRegisterMode) "Создать →" else "Войти →",
                    color = if (state.canSubmit) Accent else TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(enabled = state.canSubmit) {
                        viewModel.submit()
                    },
                )

                if (state.isLoading) {
                    Spacer(Modifier.width(12.dp))
                    CircularProgressIndicator(
                        color = Accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToggleText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = if (selected) Accent else TextSecondary,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }

    val lineWidth: Dp by animateDpAsState(
        targetValue = if (isFocused) 260.dp else 180.dp,
        animationSpec = tween(durationMillis = 300),
        label = "lineWidth",
    )

    Column {

        Text(
            text = label,
            color = if (isFocused) Accent else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
        )

        Spacer(Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
            ),
            cursorBrush = SolidColor(Accent),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .width(260.dp)
                .onFocusChanged { isFocused = it.isFocused },
        )

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(lineWidth)
                .height(1.dp)
                .background(if (isFocused) Accent else DividerColor),
        )
    }
}