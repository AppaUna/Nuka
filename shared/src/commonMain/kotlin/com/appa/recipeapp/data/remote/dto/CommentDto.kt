package com.appa.recipeapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: String,
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("user_id") val userId: String,
    val text: String,
    @SerialName("created_at") val createdAt: String,
    val profiles: ProfileNestedDto? = null,
)

@Serializable
data class ProfileNestedDto(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)