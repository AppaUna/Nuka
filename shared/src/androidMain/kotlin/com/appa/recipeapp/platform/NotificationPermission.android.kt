package com.appa.recipeapp.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

var currentActivity: ComponentActivity? = null

actual fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val activity = currentActivity ?: return
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }
}

actual fun areNotificationsEnabled(): Boolean {
    val activity = currentActivity ?: return false
    return NotificationManagerCompat.from(activity).areNotificationsEnabled()
}

actual fun openNotificationSettings() {
    val activity = currentActivity ?: return
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
    activity.startActivity(intent)
}