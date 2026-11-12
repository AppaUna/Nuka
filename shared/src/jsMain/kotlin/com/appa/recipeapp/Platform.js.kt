package com.appa.recipeapp

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "Web (Wasm)"
}