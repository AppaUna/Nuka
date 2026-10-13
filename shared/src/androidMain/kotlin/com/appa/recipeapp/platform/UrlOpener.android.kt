package com.appa.recipeapp.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

private var urlOpenerContext: Context? = null

fun initUrlOpener(context: Context) {
    urlOpenerContext = context.applicationContext
}

actual fun openExternalUrl(url: String) {
    val context = urlOpenerContext ?: return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Throwable) {
        println("DEBUG openExternalUrl: ${e.message}")
    }
}