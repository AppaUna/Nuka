package com.appa.recipeapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform