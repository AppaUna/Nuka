package com.appa.recipeapp.platform

expect fun requestNotificationPermission()
expect fun areNotificationsEnabled(): Boolean
expect fun openNotificationSettings()