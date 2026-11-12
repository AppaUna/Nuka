package com.appa.recipeapp.platform

@JsFun("(url) => { window.open(url, '_blank', 'noopener,noreferrer'); }")
private external fun jsOpenUrl(url: String)

actual fun openExternalUrl(url: String) {
    try {
        jsOpenUrl(url)
    } catch (e: Throwable) {
        println("DEBUG openExternalUrl: ${e.message}")
    }
}