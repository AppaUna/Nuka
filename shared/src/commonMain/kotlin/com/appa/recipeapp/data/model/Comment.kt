package com.appa.recipeapp.data.model

data class Comment(
    val id: String,
    val recipeId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val text: String,
    val createdAt: Long,
)