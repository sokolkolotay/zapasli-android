package ru.zapasli.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ru.zapasli.app.ui.foundation.FoundationScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Foundation)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Foundation> {
                FoundationScreen()
            }
        },
    )
}
