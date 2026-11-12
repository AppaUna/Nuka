package com.appa.recipeapp.platform

import kotlinx.browser.window

actual fun saveUserIdToStorage(userId: String) {
    window.localStorage.setItem("nuka_user_id", userId)
    println("DEBUG: user_id сохранен в localStorage: $userId")
}

actual fun savePushRequested() {
    window.localStorage.setItem("push_requested", "true")
    println("DEBUG: push_requested установлен в true")
}

actual fun areNotificationsEnabled(): Boolean {
    return try {
        val saved = window.localStorage.getItem("push_enabled")
        saved == "true"
    } catch (e: Throwable) {
        false
    }
}