package com.example.pocketcalculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun press(vararg keys: String) = keys.forEach {
        compose.onNodeWithContentDescription(it).performScrollTo().performClick()
    }
    @Test fun percentageAndErrorRecovery() {
        press("2", "0", "0", "Multiply", "1", "0", "Percent", "Equals")
        compose.onNodeWithTag("calculator_display").assertTextEquals("20")
        press("Clear calculation", "8", "Divide", "0", "Equals")
        compose.onNodeWithTag("calculator_display").assertTextEquals("Error")
        press("7")
        compose.onNodeWithTag("calculator_display").assertTextEquals("7")
    }
    @Test fun historyAndDeletion() {
        press("1", "2", "3", "Delete last digit", "Add", "3", "Equals")
        compose.onNodeWithTag("calculator_display").assertTextEquals("15")
        compose.onNodeWithText("History (1)").performScrollTo().performClick()
        compose.onNodeWithContentDescription("12 + 3 equals 15").assertIsDisplayed()
        compose.onNodeWithText("Clear history").performClick()
        compose.onNodeWithText("Completed calculations appear here.").assertIsDisplayed()
    }
}
