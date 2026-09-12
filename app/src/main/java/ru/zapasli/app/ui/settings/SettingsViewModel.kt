package ru.zapasli.app.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.zapasli.app.core.preferences.LanguageMode
import ru.zapasli.app.core.preferences.PantryFilterPreference
import ru.zapasli.app.core.preferences.ThemeMode
import ru.zapasli.app.core.preferences.UserPreferencesRepository
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val defaultPantryFilter: PantryFilterPreference = PantryFilterPreference.ALL,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val languageMode = MutableStateFlow(currentLanguageMode())

    val uiState = combine(
        preferencesRepository.userPreferences,
        languageMode,
    ) { preferences, language ->
        SettingsUiState(
            themeMode = preferences.themeMode,
            languageMode = language,
            defaultPantryFilter = preferences.defaultPantryFilter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(languageMode = currentLanguageMode()),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setDefaultPantryFilter(filter: PantryFilterPreference) {
        viewModelScope.launch { preferencesRepository.setDefaultPantryFilter(filter) }
    }

    fun setLanguageMode(mode: LanguageMode) {
        if (languageMode.value == mode) return
        languageMode.value = mode
        val locales = when (mode) {
            LanguageMode.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            LanguageMode.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            LanguageMode.RUSSIAN -> LocaleListCompat.forLanguageTags("ru")
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}

private fun currentLanguageMode(): LanguageMode {
    val language = AppCompatDelegate.getApplicationLocales()[0]?.language
    return when (language) {
        "en" -> LanguageMode.ENGLISH
        "ru" -> LanguageMode.RUSSIAN
        else -> LanguageMode.SYSTEM
    }
}
