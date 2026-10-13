package com.appa.recipeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.appa.recipeapp.platform.initAudioPlayer
import com.appa.recipeapp.platform.initFileCopier
import com.appa.recipeapp.platform.initFileReader
import com.appa.recipeapp.platform.initSettings
import com.appa.recipeapp.platform.initImageLoaderFactory
import com.appa.recipeapp.platform.initUrlOpener
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
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

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContent {
            App()
        }
    }
}