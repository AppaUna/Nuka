package com.appa.recipeapp.platform

/**
 * Открывает внешнюю ссылку в системном браузере / новой вкладке.
 * На Web — window.open(url, '_blank'), не ломает PWA-контекст.
 * На Android — Intent.ACTION_VIEW.
 * На iOS — UIApplication.sharedApplication.openURL.
 */
expect fun openExternalUrl(url: String)