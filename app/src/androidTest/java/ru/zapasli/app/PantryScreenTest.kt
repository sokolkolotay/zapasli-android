package ru.zapasli.app

import android.Manifest
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.zapasli.app.core.designsystem.theme.ZapasliTheme
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.ui.pantry.PantryEditorSheet
import ru.zapasli.app.ui.pantry.PantryItemInput
import ru.zapasli.app.ui.pantry.PantryScreen
import ru.zapasli.app.ui.pantry.PantryUiState
import ru.zapasli.app.ui.pantry.ProductLookupUiState
import ru.zapasli.app.ui.scanner.BarcodeScannerDialog
import java.time.Instant

class PantryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(
            instrumentation.targetContext.packageName,
            Manifest.permission.CAMERA,
        )
        composeRule.setContent {
            ZapasliTheme { PantryTestHarness() }
        }
    }

    @Test
    fun pantryScreenIsDisplayed() {
        composeRule.onNodeWithTag("pantry_screen").assertIsDisplayed()
    }

    @Test
    fun emptyPantryOpensProductEditor() {
        waitForTag("add_first_product")
        composeRule.onNodeWithTag("add_first_product").performClick()
        composeRule.onNodeWithTag("product_editor").assertIsDisplayed()
    }

    @Test
    fun scannerCanOpenAndReturnToManualEntry() {
        waitForTag("add_first_product")
        composeRule.onNodeWithTag("add_first_product").performClick()
        composeRule.onNodeWithTag("scan_barcode").performClick()

        waitForTag("barcode_scanner")
        composeRule.onNodeWithTag("barcode_scanner").assertIsDisplayed()
        composeRule.onNodeWithTag("scanner_manual_entry").performClick()
        composeRule.onNodeWithTag("product_editor").assertIsDisplayed()
    }

    @Test
    fun productCanBeAddedEditedAndDeleted() {
        waitForTag("add_first_product")
        composeRule.onNodeWithTag("add_first_product").performClick()
        composeRule.onNodeWithTag("product_name_input").performTextInput("Milk")
        composeRule.onNodeWithTag("save_product")
            .performScrollTo()
            .performClick()

        waitForText("product_name", "Milk")

        composeRule.onNodeWithTag("edit_product", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("product_name_input").performTextClearance()
        composeRule.onNodeWithTag("product_name_input").performTextInput("Yogurt")
        composeRule.onNodeWithTag("save_product")
            .performScrollTo()
            .performClick()

        waitForText("product_name", "Yogurt")

        composeRule.onNodeWithTag("delete_product", useUnmergedTree = true).performClick()
        waitForTag("empty_pantry")
        composeRule.onNodeWithTag("empty_pantry").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(tag: String, text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag("pantry_list").performScrollToIndex(3)
                composeRule.onNodeWithTag(tag, useUnmergedTree = true).assertTextEquals(text)
            }.isSuccess
        }
    }
}

@Composable
private fun PantryTestHarness() {
    val items = remember { mutableStateListOf<PantryItem>() }
    var editorItem by remember { mutableStateOf<PantryItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    PantryScreen(
        state = PantryUiState(
            isLoading = false,
            items = items.toList(),
            totalItemCount = items.size,
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onAddProduct = {
            editorItem = null
            showEditor = true
        },
        onEditProduct = {
            editorItem = it
            showEditor = true
        },
        onDeleteProduct = { item -> items.removeAll { it.id == item.id } },
        onFilterSelected = {},
        onRetry = {},
        userDisplayName = "Test User",
        isSessionOffline = false,
    )

    if (showEditor) {
        PantryEditorSheet(
            item = editorItem,
            scannedBarcode = null,
            productLookup = ProductLookupUiState.Idle,
            onScanBarcode = { showScanner = true },
            onBarcodeInputChanged = {},
            onDismiss = { showEditor = false },
            onSave = { input ->
                val saved = input.toPantryItem(items.firstOrNull { it.id == input.id })
                items.removeAll { it.id == saved.id }
                items.add(saved)
                showEditor = false
            },
        )
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onBarcodeDetected = { showScanner = false },
            onDismiss = { showScanner = false },
        )
    }
}

private fun PantryItemInput.toPantryItem(existing: PantryItem?): PantryItem {
    val now = Instant.now()
    return PantryItem(
        id = id ?: "test-item",
        name = name.trim(),
        barcode = barcode,
        quantity = quantity,
        unit = unit,
        storageLocation = storageLocation,
        expiresOn = expiresOn,
        nutritionPer100g = nutritionPer100g,
        createdAt = existing?.createdAt ?: now,
        updatedAt = now,
    )
}
