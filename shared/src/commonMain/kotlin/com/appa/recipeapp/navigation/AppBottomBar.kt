package com.appa.recipeapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.ic_add
import recipeapp.shared.generated.resources.ic_favorite
import recipeapp.shared.generated.resources.ic_home
import recipeapp.shared.generated.resources.ic_person

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (Routes) -> Unit,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    ),
                )
            )
            .padding(top = 10.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarItem(
                label = "Главная",
                selected = currentRoute == Routes.Home.route,
                onClick = { onNavigate(Routes.Home) },
                iconRes = Res.drawable.ic_home,
            )

            BarItem(
                label = "Избранное",
                selected = currentRoute == Routes.Favorites.route,
                onClick = { onNavigate(Routes.Favorites) },
                iconRes = Res.drawable.ic_favorite,
            )

            BarItem(
                label = "Добавить",
                selected = false,
                onClick = onCreateClick,
                iconRes = Res.drawable.ic_add,
                forceAccent = true,
            )

            BarItem(
                label = "Профиль",
                selected = currentRoute == Routes.Profile.route,
                onClick = { onNavigate(Routes.Profile) },
                iconRes = Res.drawable.ic_person,
            )
        }
    }
}

@Composable
private fun BarItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    forceAccent: Boolean = false,
) {
    val iconColor = if (forceAccent || selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val textColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val iconSize = if (forceAccent) 28.dp else 24.dp

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(iconSize),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
        )
    }
}