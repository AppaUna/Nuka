package com.appa.recipeapp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = document.getElementById("compose-root")
        ?: error("Не найден #compose-root в index.html")

    ComposeViewport(root) {
        App()
    }
}