package com.yasashny.fortera.feature.walletselector.settings.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.walletselector.R
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsIntent
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WalletSettingsLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val contentState = WalletSettingsState.Content(
        walletId = "w1",
        name = "Wallet 1",
    )

    @Test
    fun titleAndBackArrowAreDisplayed() {
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(state = contentState, onIntent = {})
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.wallet_settings_back_cd))
            .assertIsDisplayed()
    }

    @Test
    fun nameFieldShowsCurrentNameAndSaveIsEnabled() {
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(state = contentState, onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("Wallet 1").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_save))
            .assertIsEnabled()
    }

    @Test
    fun saveIsDisabledWhenNameIsBlank() {
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(
                    state = contentState.copy(name = ""),
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_save))
            .assertIsNotEnabled()
    }

    @Test
    fun editingNameEmitsNameChanged() {
        val intents = mutableListOf<WalletSettingsIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(state = contentState, onIntent = { intents += it })
            }
        }

        composeTestRule.onNodeWithText("Wallet 1").performTextReplacement("Renamed")

        assertTrue(
            "Expected a NameChanged intent with the new value, got $intents",
            intents.any { it is WalletSettingsIntent.NameChanged && it.value == "Renamed" },
        )
    }

    @Test
    fun tappingSaveEmitsSaveClicked() {
        val intents = mutableListOf<WalletSettingsIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(state = contentState, onIntent = { intents += it })
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_save))
            .performClick()

        assertEquals(listOf(WalletSettingsIntent.SaveClicked), intents)
    }

    @Test
    fun tappingDeleteEmitsDeleteClicked() {
        val intents = mutableListOf<WalletSettingsIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(state = contentState, onIntent = { intents += it })
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_delete))
            .performClick()

        assertEquals(listOf(WalletSettingsIntent.DeleteClicked), intents)
    }

    @Test
    fun deleteDialogVisibleWhenStateRequests() {
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(
                    state = contentState.copy(showDeleteDialog = true),
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_delete_dialog_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_delete_dialog_message))
            .assertIsDisplayed()
    }

    @Test
    fun dialogConfirmEmitsConfirmDelete() {
        val intents = mutableListOf<WalletSettingsIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(
                    state = contentState.copy(showDeleteDialog = true),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_delete_dialog_confirm))
            .performClick()

        assertEquals(listOf(WalletSettingsIntent.ConfirmDelete), intents)
    }

    @Test
    fun dialogCancelEmitsDismissDeleteDialog() {
        val intents = mutableListOf<WalletSettingsIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(
                    state = contentState.copy(showDeleteDialog = true),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.wallet_settings_delete_dialog_cancel))
            .performClick()

        assertEquals(listOf(WalletSettingsIntent.DismissDeleteDialog), intents)
    }

    @Test
    fun backArrowEmitsBackClicked() {
        val intents = mutableListOf<WalletSettingsIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                WalletSettingsLayout(state = contentState, onIntent = { intents += it })
            }
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.wallet_settings_back_cd))
            .performClick()

        assertEquals(listOf(WalletSettingsIntent.BackClicked), intents)
    }
}
