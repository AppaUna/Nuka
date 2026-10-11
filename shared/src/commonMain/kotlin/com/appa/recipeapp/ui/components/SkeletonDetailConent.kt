package com.appa.recipeapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Скелетон Detail — фото → название → метрики → секции.
 */
@Composable
fun SkeletonDetailContent() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // Фото рецепта
        item {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                cornerRadius = 0.dp,
            )
        }

        // Карточка с названием (перекрывает фото снизу)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(30.dp),
                    cornerRadius = 6.dp,
                )
                Spacer(Modifier.height(16.dp))
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp),
                    cornerRadius = 4.dp,
                )
                Spacer(Modifier.height(20.dp))
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    cornerRadius = 8.dp,
                )
            }
        }

        // Секция "Ингредиенты"
        item { Spacer(Modifier.height(20.dp)) }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp),
                    cornerRadius = 4.dp,
                )
                Spacer(Modifier.height(24.dp))
                repeat(4) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        cornerRadius = 4.dp,
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Секция "Приготовление"
        item { Spacer(Modifier.height(40.dp)) }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp),
                    cornerRadius = 4.dp,
                )
                Spacer(Modifier.height(24.dp))
                repeat(3) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        cornerRadius = 8.dp,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}