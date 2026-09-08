package ru.zapasli.app.core.preferences

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
