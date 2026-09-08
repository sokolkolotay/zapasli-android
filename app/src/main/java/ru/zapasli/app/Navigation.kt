package ru.zapasli.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ru.zapasli.app.ui.pantry.PantryRoute
import ru.zapasli.app.ui.pantry.PantryViewModel

@Composable
fun MainNavigation(pantryViewModel: PantryViewModel) {
    val backStack = rememberNavBackStack(Pantry)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Pantry> {
                PantryRoute(viewModel = pantryViewModel)
            }
        },
    )
}
