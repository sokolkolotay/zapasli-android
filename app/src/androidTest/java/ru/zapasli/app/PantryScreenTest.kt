package ru.zapasli.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PantryScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun grantCameraPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(
            instrumentation.targetContext.packageName,
            Manifest.permission.CAMERA,
        )
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
    fun productCanBeAddedEditedPersistedAndDeleted() {
        waitForTag("add_first_product")
        composeRule.onNodeWithTag("add_first_product").performClick()
        composeRule.onNodeWithTag("product_name_input").performTextInput("Milk")
        composeRule.onNodeWithTag("save_product")
            .performScrollTo()
            .performClick()

        waitForText("product_name", "Milk")

        composeRule.activityRule.scenario.recreate()
        waitForText("product_name", "Milk")

        composeRule.onNodeWithTag("edit_product").performClick()
        composeRule.onNodeWithTag("product_name_input").performTextClearance()
        composeRule.onNodeWithTag("product_name_input").performTextInput("Yogurt")
        composeRule.onNodeWithTag("save_product")
            .performScrollTo()
            .performClick()

        waitForText("product_name", "Yogurt")

        composeRule.onNodeWithTag("delete_product").performClick()
        composeRule.onNodeWithTag("confirm_delete").performClick()
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
                composeRule.onNodeWithTag(tag).assertTextEquals(text)
            }.isSuccess
        }
    }
}
