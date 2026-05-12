package com.yasashny.fortera.feature.importwallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.importwallet.R
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletIntent
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImportWalletLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun emptyStateDisablesImportButton() {
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(),
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_import_button))
            .assertIsNotEnabled()
    }

    @Test
    fun filledStateEnablesImportButton() {
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(
                        name = "My wallet",
                        seedPhrase = "abandon abandon abandon",
                    ),
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_import_button))
            .assertIsEnabled()
    }

    @Test
    fun typingIntoNameFieldEmitsNameChanged() {
        val intents = mutableListOf<ImportWalletIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_name_placeholder))
            .performTextInput("X")

        assertTrue(
            "Expected at least one NameChanged intent, got $intents",
            intents.any { it is ImportWalletIntent.NameChanged && it.value == "X" },
        )
    }

    @Test
    fun typingIntoSeedFieldEmitsSeedPhraseChanged() {
        val intents = mutableListOf<ImportWalletIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_seed_phrase_placeholder))
            .performTextInput("X")

        assertTrue(
            "Expected at least one SeedPhraseChanged intent, got $intents",
            intents.any { it is ImportWalletIntent.SeedPhraseChanged && it.value == "X" },
        )
    }

    @Test
    fun tappingImportEmitsImportClicked() {
        val intents = mutableListOf<ImportWalletIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(name = "n", seedPhrase = "s"),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_import_button))
            .performClick()

        assertEquals(listOf(ImportWalletIntent.ImportClicked), intents)
    }

    @Test
    fun tappingBackEmitsBackClicked() {
        val intents = mutableListOf<ImportWalletIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.import_wallet_back_cd))
            .performClick()

        assertEquals(listOf(ImportWalletIntent.BackClicked), intents)
    }

    @Test
    fun fieldErrorMessagesAreDisplayed() {
        composeTestRule.setContent {
            ForteraTheme {
                ImportWalletLayout(
                    state = ImportWalletState.Content(
                        nameError = UiText.of(R.string.import_wallet_error_name_empty),
                        seedPhraseError = UiText.of(R.string.import_wallet_error_seed_empty),
                    ),
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_error_name_empty))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.import_wallet_error_seed_empty))
            .assertIsDisplayed()
    }
}
