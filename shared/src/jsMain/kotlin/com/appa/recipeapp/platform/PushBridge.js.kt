package com.appa.recipeapp.platform

actual fun enablePush() {
    js("window.requestPushPermission()")
}

actual fun disablePush() {
    js("window.disablePushPermission()")
}