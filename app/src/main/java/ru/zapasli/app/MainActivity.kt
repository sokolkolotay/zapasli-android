package ru.zapasli.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.zapasli.app.core.designsystem.theme.ZapasliTheme
import ru.zapasli.app.ui.auth.AuthViewModel
import ru.zapasli.app.ui.pantry.PantryViewModel
import ru.zapasli.app.core.preferences.ThemeMode
import ru.zapasli.app.ui.settings.SettingsViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val pantryViewModel: PantryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (settingsState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            ZapasliTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ZapasliRoot(
                        authViewModel = authViewModel,
                        pantryViewModel = pantryViewModel,
                        settingsViewModel = settingsViewModel,
                    )
                }
            }
        }
    }
}
