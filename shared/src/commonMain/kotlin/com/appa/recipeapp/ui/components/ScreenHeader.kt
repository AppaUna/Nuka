package com.appa.recipeapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary

/**
 * Единый заголовок экрана — используется на Home, Favorites, Profile.
 * Высота адаптивная — subtitle не пропадает, даже если title длинный.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onLogoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    topPadding: Dp = 16.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = topPadding, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        AppLogo(
            size = 56.dp,
            modifier = if (onLogoClick != null) {
                Modifier.clickable(onClick = onLogoClick)
            } else Modifier,
        )
    }
}