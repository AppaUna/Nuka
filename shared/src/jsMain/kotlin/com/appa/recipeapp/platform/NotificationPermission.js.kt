package com.appa.recipeapp.platform

actual fun requestNotificationPermission() {
    println("DEBUG: Запрос уведомлений отключен в Kotlin (делаем через JS)")
}



actual fun openNotificationSettings() {}