package ru.zapasli.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ru.zapasli.app.ui.auth.AuthLoadingScreen
import ru.zapasli.app.ui.auth.AuthScreen
import ru.zapasli.app.ui.auth.AuthViewModel
import ru.zapasli.app.ui.pantry.PantryRoute
import ru.zapasli.app.ui.pantry.PantryViewModel

@Composable
fun ZapasliRoot(
    authViewModel: AuthViewModel,
    pantryViewModel: PantryViewModel,
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val session = authState.session

    when {
        authState.isRestoring -> AuthLoadingScreen()
        session == null -> AuthScreen(
            state = authState,
            onModeSelected = authViewModel::setMode,
            onEmailChanged = authViewModel::setEmail,
            onPasswordChanged = authViewModel::setPassword,
            onDisplayNameChanged = authViewModel::setDisplayName,
            onSubmit = authViewModel::submit,
        )
        else -> MainNavigation(
            pantryViewModel = pantryViewModel,
            userDisplayName = session.user.displayName,
            isSessionOffline = authState.isOfflineSession,
            onLogout = authViewModel::logout,
        )
    }
}

@Composable
private fun MainNavigation(
    pantryViewModel: PantryViewModel,
    userDisplayName: String,
    isSessionOffline: Boolean,
    onLogout: () -> Unit,
) {
    val backStack = rememberNavBackStack(Pantry)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Pantry> {
                PantryRoute(
                    viewModel = pantryViewModel,
                    userDisplayName = userDisplayName,
                    isSessionOffline = isSessionOffline,
                    onLogout = onLogout,
                )
            }
        },
    )
}
