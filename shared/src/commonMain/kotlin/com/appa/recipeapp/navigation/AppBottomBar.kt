package com.appa.recipeapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.NavInactive
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.ic_add
import recipeapp.shared.generated.resources.ic_favorite
import recipeapp.shared.generated.resources.ic_home
import recipeapp.shared.generated.resources.ic_person
import androidx.compose.foundation.layout.Box
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (Routes) -> Unit,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
        // Тонкая линия сверху
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Главная
            BarItem(
                label = "Главная",
                selected = currentRoute == Routes.Home.route,
                onClick = { onNavigate(Routes.Home) },
                iconRes = Res.drawable.ic_home,
            )

            // Избранное
            BarItem(
                label = "Избранное",
                selected = currentRoute == Routes.Favorites.route,
                onClick = { onNavigate(Routes.Favorites) },
                iconRes = Res.drawable.ic_favorite,
            )

            // Добавить (всегда акцент)
            BarItem(
                label = "Добавить",
                selected = false,
                onClick = onCreateClick,
                iconRes = Res.drawable.ic_add,
                forceAccent = true,
            )

            // Профиль
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
    val iconColor = if (forceAccent || selected) Accent else NavInactive
    val textColor = if (selected) Accent else NavInactive
    val iconSize = if (forceAccent) 28.dp else 24.dp

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
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