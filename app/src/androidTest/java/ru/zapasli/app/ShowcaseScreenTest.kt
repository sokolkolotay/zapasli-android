package ru.zapasli.app

import android.os.ParcelFileDescriptor
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import ru.zapasli.app.core.designsystem.theme.ZapasliTheme
import ru.zapasli.app.core.preferences.LanguageMode
import ru.zapasli.app.core.preferences.PantryFilterPreference
import ru.zapasli.app.core.preferences.ThemeMode
import ru.zapasli.app.domain.pantry.NutritionPer100g
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.QuantityUnit
import ru.zapasli.app.domain.pantry.StorageLocation
import ru.zapasli.app.ui.details.ProductDetailsScreen
import ru.zapasli.app.ui.pantry.PantryScreen
import ru.zapasli.app.ui.pantry.PantryUiState
import ru.zapasli.app.ui.settings.SettingsScreen
import ru.zapasli.app.ui.settings.SettingsUiState
import java.time.Instant
import java.time.LocalDate

class ShowcaseScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pantryShowcaseIsRendered() {
        val products = sampleProducts()
        var openedDetails = false
        composeRule.setContent {
            ZapasliTheme {
                PantryScreen(
                    state = PantryUiState(
                        isLoading = false,
                        items = products,
                        allItems = products,
                        totalItemCount = products.size,
                        attentionItemCount = 1,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAddProduct = {},
                    onEditProduct = {},
                    onDeleteProduct = {},
                    onFilterSelected = {},
                    onRetry = {},
                    userDisplayName = "Илья",
                    isSessionOffline = false,
                    onProductClick = { openedDetails = true },
                )
            }
        }
        composeRule.onNodeWithTag("pantry_screen").assertIsDisplayed()
        captureScreen("zapasli-pantry.png")
        composeRule.onNodeWithTag("pantry_list").performScrollToIndex(3)
        composeRule.onNodeWithTag("product_card_milk").performClick()
        assertTrue(openedDetails)
    }

    @Test
    fun detailsShowcaseIsRendered() {
        composeRule.setContent {
            ZapasliTheme {
                ProductDetailsScreen(
                    item = sampleProducts().first(),
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithTag("product_details_screen").assertIsDisplayed()
        captureScreen("zapasli-details.png")
    }

    @Test
    fun settingsShowcaseIsRendered() {
        var selectedTheme = ThemeMode.SYSTEM
        var selectedLanguage = LanguageMode.SYSTEM
        var selectedFilter = PantryFilterPreference.ALL
        composeRule.setContent {
            ZapasliTheme {
                SettingsScreen(
                    state = SettingsUiState(
                        themeMode = ThemeMode.SYSTEM,
                        languageMode = LanguageMode.SYSTEM,
                        defaultPantryFilter = PantryFilterPreference.ALL,
                    ),
                    userDisplayName = "Илья",
                    onBack = {},
                    onThemeSelected = { selectedTheme = it },
                    onLanguageSelected = { selectedLanguage = it },
                    onFilterSelected = { selectedFilter = it },
                    onLogout = {},
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        captureScreen("zapasli-settings.png")
        composeRule.onNodeWithText("Dark").performClick()
        composeRule.onNodeWithText("Russian").performClick()
        composeRule.onNodeWithText("No date").performScrollTo().performClick()
        assertEquals(ThemeMode.DARK, selectedTheme)
        assertEquals(LanguageMode.RUSSIAN, selectedLanguage)
        assertEquals(PantryFilterPreference.NO_DATE, selectedFilter)
    }

    private fun captureScreen(fileName: String) {
        composeRule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val descriptor = instrumentation.uiAutomation.executeShellCommand(
            "screencap -p /sdcard/Download/$fileName",
        )
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    private fun sampleProducts(): List<PantryItem> {
        val now = Instant.parse("2026-09-12T06:00:00Z")
        val today = LocalDate.now()
        return listOf(
            PantryItem(
                id = "milk",
                name = "Молоко 3,2%",
                barcode = "4600000000001",
                quantity = 900.0,
                unit = QuantityUnit.MILLILITER,
                storageLocation = StorageLocation.FRIDGE,
                expiresOn = today.plusDays(2),
                nutritionPer100g = NutritionPer100g(60.0, 3.0, 3.2, 4.7),
                createdAt = now,
                updatedAt = now,
            ),
            PantryItem(
                id = "eggs",
                name = "Яйца С0",
                barcode = "4600000000002",
                quantity = 10.0,
                unit = QuantityUnit.PIECE,
                storageLocation = StorageLocation.FRIDGE,
                expiresOn = today.plusDays(8),
                nutritionPer100g = NutritionPer100g(157.0, 12.7, 10.9, 0.7),
                createdAt = now,
                updatedAt = now,
            ),
            PantryItem(
                id = "apples",
                name = "Яблоки",
                barcode = null,
                quantity = 1.5,
                unit = QuantityUnit.KILOGRAM,
                storageLocation = StorageLocation.PANTRY,
                expiresOn = null,
                nutritionPer100g = NutritionPer100g(52.0, 0.3, 0.2, 14.0),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
