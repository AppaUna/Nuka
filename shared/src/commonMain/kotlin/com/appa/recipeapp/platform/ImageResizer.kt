package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap

expect fun resizeImageBitmap(image: ImageBitmap, maxSize: Int): ImageBitmap