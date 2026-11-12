package com.appa.recipeapp

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FcmHelper {
    fun fetchTokenAndSave(onTokenReceived: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(2000) // Ждём, чтобы Firebase инициализировался
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Token: $token")
                onTokenReceived(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to get token", e)
            }
        }
    }
}