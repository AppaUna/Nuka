package com.appa.recipeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonHomeContent() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item { SkeletonScreenHeader() }
        item { SkeletonSearchBar() }
        item { SkeletonTagRow() }

        item { Spacer(Modifier.height(20.dp)) }
        item { SkeletonSectionHeader() }
        item { Spacer(Modifier.height(12.dp)) }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(count = 3) { SkeletonRecipeCardLarge() }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    cornerRadius = 16.dp,
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item { SkeletonSectionHeader() }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(count = 3) { SkeletonRecipeCardMedium() }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item { SkeletonSectionHeader() }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(count = 3) { SkeletonRecipeCardMedium() }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item { SkeletonSectionHeader() }
        item { Spacer(Modifier.height(12.dp)) }
        item { SkeletonRecipeCardList() }
        item { SkeletonRecipeCardList() }
        item { SkeletonRecipeCardList() }
    }
}