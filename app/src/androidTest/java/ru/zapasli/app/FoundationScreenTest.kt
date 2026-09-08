package ru.zapasli.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class FoundationScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun foundationScreenIsDisplayed() {
        composeRule.onNodeWithTag("foundation_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Zapasli").assertIsDisplayed()
    }
}
