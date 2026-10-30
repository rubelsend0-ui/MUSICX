package com.soniqxpro.model

enum class SortOrder(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    RECENTLY_ADDED("Recently added")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentIndex: Int = 0,
    val sortOrder: SortOrder = SortOrder.TITLE
)
