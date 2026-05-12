package com.yasashny.fortera.feature.main.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.yasashny.fortera.core.common.currency.Currency
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.ui.currency.FiatDisplay
import com.yasashny.fortera.core.ui.currency.LocalFiat
import com.yasashny.fortera.feature.main.R
import com.yasashny.fortera.feature.main.presentation.BalancesState
import com.yasashny.fortera.feature.main.presentation.Banner
import com.yasashny.fortera.feature.main.presentation.MainIntent
import com.yasashny.fortera.feature.main.presentation.MainState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class MainLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val fiatUsd = FiatDisplay(Currency.USD, rateFromUsd = 1.0)

    private val btcBalance = TokenBalance(
        token = TokenDefinition(
            id = "bitcoin",
            name = "Bitcoin",
            symbol = "BTC",
            network = BlockchainNetwork.BITCOIN,
            decimals = 8,
            contractAddress = null,
            coingeckoId = "bitcoin",
            isDefault = true,
        ),
        balance = BigDecimal("0.25"),
        priceUsd = 100.0,
        changePercent24h = 1.5,
    )

    @Test
    fun walletNameAndTotalRenderInReadyState() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(
                            walletName = "Primary wallet",
                            balances = BalancesState.Ready(
                                totalUsd = 1234.56,
                                tokens = listOf(btcBalance),
                                isStale = false,
                            ),
                        ),
                        onIntent = {},
                    )
                }
            }
        }

        composeTestRule.onAllNodesWithText("Primary wallet").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        // Fiat formatter uses comma as decimal separator, space as group separator.
        composeTestRule.onAllNodesWithText("$1 234,56").onFirst().assertIsDisplayed()
    }

    @Test
    fun quickActionsAndManageTokensTriggerIntents() {
        val intents = mutableListOf<MainIntent>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(
                            walletName = "Primary",
                            balances = BalancesState.Ready(
                                totalUsd = 0.0,
                                tokens = listOf(btcBalance),
                                isStale = false,
                            ),
                        ),
                        onIntent = { intents += it },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.main_send)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.main_receive)).performClick()
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_manage_tokens))
            .performClick()

        assertEquals(
            listOf(MainIntent.OpenSend, MainIntent.OpenReceive, MainIntent.OpenManageTokens),
            intents,
        )
    }

    @Test
    fun settingsIconEmitsOpenSettings() {
        val intents = mutableListOf<MainIntent>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(walletName = "Primary"),
                        onIntent = { intents += it },
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.main_settings_cd))
            .performClick()

        assertEquals(listOf(MainIntent.OpenSettings), intents)
    }

    @Test
    fun walletNameClickEmitsOpenWalletSelector() {
        val intents = mutableListOf<MainIntent>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(walletName = "Primary"),
                        onIntent = { intents += it },
                    )
                }
            }
        }

        composeTestRule.onAllNodesWithText("Primary").onFirst().performClick()

        assertTrue(
            "Expected OpenWalletSelector somewhere in $intents",
            MainIntent.OpenWalletSelector in intents,
        )
    }

    @Test
    fun tokenRowClickEmitsOpenTokenDetailsWithId() {
        val intents = mutableListOf<MainIntent>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(
                            walletName = "Primary",
                            balances = BalancesState.Ready(
                                totalUsd = 25.0,
                                tokens = listOf(btcBalance),
                                isStale = false,
                            ),
                        ),
                        onIntent = { intents += it },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Bitcoin").performClick()

        assertEquals(listOf(MainIntent.OpenTokenDetails("bitcoin")), intents)
    }

    @Test
    fun genericErrorBannerIsDisplayed() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(
                            walletName = "Primary",
                            banner = Banner.GenericError,
                        ),
                        onIntent = {},
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.main_load_error))
            .assertIsDisplayed()
    }

    @Test
    fun networksUnavailableBannerMentionsNetwork() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFiat provides fiatUsd) {
                ForteraTheme {
                    MainLayout(
                        state = MainState(
                            walletName = "Primary",
                            banner = Banner.NetworksUnavailable(setOf("Ethereum")),
                        ),
                        onIntent = {},
                    )
                }
            }
        }

        val expected = context.getString(R.string.main_network_unavailable_single, "Ethereum")
        composeTestRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
