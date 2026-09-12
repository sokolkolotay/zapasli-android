package ru.zapasli.app.core.preferences

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultPantryFilter: PantryFilterPreference = PantryFilterPreference.ALL,
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class LanguageMode {
    SYSTEM,
    ENGLISH,
    RUSSIAN,
}

enum class PantryFilterPreference {
    ALL,
    EXPIRING_SOON,
    EXPIRED,
    NO_DATE,
}
