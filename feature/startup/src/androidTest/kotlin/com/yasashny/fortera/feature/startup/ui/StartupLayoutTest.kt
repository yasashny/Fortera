package com.yasashny.fortera.feature.startup.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.startup.R
import com.yasashny.fortera.feature.startup.presentation.StartupIntent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StartupLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun rendersCreateAndImportCards() {
        composeTestRule.setContent {
            ForteraTheme {
                StartupLayout(onIntent = {})
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.startup_create_new_wallet))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.startup_import_wallet))
            .assertIsDisplayed()
    }

    @Test
    fun tappingCardsEmitsCorrespondingIntents() {
        val intents = mutableListOf<StartupIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                StartupLayout(onIntent = { intents += it })
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.startup_create_new_wallet))
            .performClick()
        composeTestRule
            .onNodeWithText(context.getString(R.string.startup_import_wallet))
            .performClick()

        assertEquals(
            listOf(StartupIntent.CreateWalletClicked, StartupIntent.ImportWalletClicked),
            intents,
        )
    }
}
