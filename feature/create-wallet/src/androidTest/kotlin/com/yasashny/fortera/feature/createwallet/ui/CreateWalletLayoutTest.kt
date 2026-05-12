package com.yasashny.fortera.feature.createwallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.createwallet.R
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletIntent
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreateWalletLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val sampleWords = listOf(
        "abandon", "ability", "able", "about", "above", "absent",
        "absorb", "abstract", "absurd", "abuse", "access", "accident",
    )

    @Test
    fun loadingStateShowsTitleAndBackButton() {
        composeTestRule.setContent {
            ForteraTheme {
                CreateWalletLayout(
                    state = CreateWalletState.Loading,
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_wallet_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.create_wallet_back_cd))
            .assertIsDisplayed()
    }

    @Test
    fun seedPhraseStateRendersAllWordsAndCreateButton() {
        composeTestRule.setContent {
            ForteraTheme {
                CreateWalletLayout(
                    state = CreateWalletState.ShowSeedPhrase(seedPhrase = sampleWords),
                    onIntent = {},
                )
            }
        }

        sampleWords.forEach { word ->
            composeTestRule.onNodeWithText(word).assertIsDisplayed()
        }
        composeTestRule
            .onNodeWithText(context.getString(R.string.create_wallet_create_button))
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun tappingCreateEmitsCreateClickedWithNameTemplate() {
        val intents = mutableListOf<CreateWalletIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                CreateWalletLayout(
                    state = CreateWalletState.ShowSeedPhrase(seedPhrase = sampleWords),
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_wallet_create_button))
            .performClick()

        val template = context.getString(R.string.create_wallet_name_template)
        assertTrue(
            "Expected one CreateClicked intent but got $intents",
            intents.singleOrNull() == CreateWalletIntent.CreateClicked(nameTemplate = template),
        )
    }

    @Test
    fun tappingBackEmitsBackClicked() {
        val intents = mutableListOf<CreateWalletIntent>()
        composeTestRule.setContent {
            ForteraTheme {
                CreateWalletLayout(
                    state = CreateWalletState.Loading,
                    onIntent = { intents += it },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.create_wallet_back_cd))
            .performClick()

        assertTrue(intents.singleOrNull() == CreateWalletIntent.BackClicked)
    }

    @Test
    fun creatingDisablesTheButton() {
        composeTestRule.setContent {
            ForteraTheme {
                CreateWalletLayout(
                    state = CreateWalletState.ShowSeedPhrase(
                        seedPhrase = sampleWords,
                        isCreating = true,
                    ),
                    onIntent = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.create_wallet_back_cd))
            .assertIsDisplayed()
    }
}
