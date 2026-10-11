package com.appa.recipeapp.utils

/**
 * Нечёткий поиск — устойчив к опечаткам.
 * Возвращает true, если строка [text] похожа на [query].
 *
 * Логика: расстояние Левенштейна между фрагментом [text] длины query.length
 * и query не больше допуска.
 */
fun fuzzyMatches(text: String, query: String, tolerance: Int = 2): Boolean {
    if (query.isBlank()) return true

    val textLower = text.lowercase()
    val queryLower = query.lowercase().trim()

    // 1. Прямое вхождение — приоритет
    if (textLower.contains(queryLower)) return true

    // 2. Нечёткий поиск по словам
    // Разбиваем текст на слова и проверяем каждое
    val words = textLower.split(" ", "-", "«", "»", ",", ".")
    val queryWords = queryLower.split(" ").filter { it.isNotBlank() }

    // Все слова запроса должны найтись среди слов текста
    return queryWords.all { qWord ->
        words.any { word ->
            levenshtein(word, qWord) <= tolerance ||
                    word.startsWith(qWord.take(3))   // Совпадение по первым 3 буквам
        }
    }
}

/**
 * Расстояние Левенштейна — минимальное количество правок
 * (вставка, удаление, замена символа), чтобы превратить [a] в [b].
 */
private fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    val dp = Array(a.length + 1) { IntArray(b.length + 1) }

    for (i in 0..a.length) dp[i][0] = i
    for (j in 0..b.length) dp[0][j] = j

    for (i in 1..a.length) {
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,       // удаление
                dp[i][j - 1] + 1,       // вставка
                dp[i - 1][j - 1] + cost // замена
            )
        }
    }
    return dp[a.length][b.length]
}