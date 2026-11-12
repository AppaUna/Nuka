package com.appa.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import coil3.SingletonImageLoader
import com.appa.recipeapp.platform.createImageLoader
import com.appa.recipeapp.platform.currentActivity
import com.appa.recipeapp.platform.initAudioPlayer
import com.appa.recipeapp.platform.initFileCopier
import com.appa.recipeapp.platform.initFileReader
import com.appa.recipeapp.platform.initImageLoaderFactory
import com.appa.recipeapp.platform.initSettings
import com.appa.recipeapp.platform.initUrlOpener
import com.appa.recipeapp.platform.saveFcmTokenLocally
import com.appa.recipeapp.platform.sendFcmTokenToSupabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    private val _notificationUrl = MutableStateFlow<String?>(null)
    private val notificationUrl: StateFlow<String?> = _notificationUrl.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        initFileCopier(this)
        initSettings(this)
        initAudioPlayer(this)
        initFileReader(this)
        initImageLoaderFactory(this)
        initUrlOpener(this)
        currentActivity = this

        SingletonImageLoader.setSafe { context ->
            createImageLoader()
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        _notificationUrl.value = intent.getStringExtra("url")
        Log.d("FCM", "onCreate Intent url: ${_notificationUrl.value}")

        setContent {
            val url by notificationUrl.collectAsState()
            App(initialUrl = url ?: "/")
        }

        FcmHelper.fetchTokenAndSave { token ->
            Log.d("FCM", "Готово, токен: $token")
            saveFcmTokenLocally(token)
            sendFcmTokenToSupabase(token)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val url = intent.getStringExtra("url")
        Log.d("FCM", "onNewIntent Intent url: $url")
        if (url != null) {
            _notificationUrl.value = url
        }
    }
}