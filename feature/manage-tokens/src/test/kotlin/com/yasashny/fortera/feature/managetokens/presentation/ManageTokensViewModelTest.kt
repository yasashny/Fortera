package com.yasashny.fortera.feature.managetokens.presentation

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageTokensViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val tokenRepository: TokenRepository = mockk(relaxUnitFun = true)
    private val walletInteractor: WalletInteractor = mockk()

    private val activeWallet = Wallet(id = "w1", name = "Main", createdAt = 0L)

    private val btc = TokenDefinition(
        id = "bitcoin",
        name = "Bitcoin",
        symbol = "BTC",
        network = BlockchainNetwork.BITCOIN,
        decimals = 8,
        contractAddress = null,
        coingeckoId = "bitcoin",
        isDefault = true,
    )
    private val eth = TokenDefinition(
        id = "ethereum",
        name = "Ethereum",
        symbol = "ETH",
        network = BlockchainNetwork.ETHEREUM,
        decimals = 18,
        contractAddress = null,
        coingeckoId = "ethereum",
        isDefault = true,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tokens combined with enabled flags appear in state`() = runTest(dispatcher) {
        every { tokenRepository.observeAllTokens() } returns flowOf(listOf(btc, eth))
        every { tokenRepository.observeEnabledTokenIds("w1") } returns flowOf(setOf("bitcoin"))

        val viewModel = ManageTokensViewModel(tokenRepository, walletInteractor)
        advanceUntilIdle()

        val state = viewModel.currentState
        assertFalse(state.isLoading)
        assertEquals(2, state.tokens.size)
        assertTrue(state.tokens.first { it.token.id == "bitcoin" }.isEnabled)
        assertFalse(state.tokens.first { it.token.id == "ethereum" }.isEnabled)
    }

    @Test
    fun `Toggle enables a disabled token`() = runTest(dispatcher) {
        val enabledIds = MutableStateFlow(setOf("bitcoin"))
        every { tokenRepository.observeAllTokens() } returns flowOf(listOf(btc, eth))
        every { tokenRepository.observeEnabledTokenIds("w1") } returns enabledIds
        coEvery { tokenRepository.setTokenEnabled("w1", "ethereum", true) } coAnswers {
            enabledIds.value = enabledIds.value + "ethereum"
        }
        val viewModel = ManageTokensViewModel(tokenRepository, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(ManageTokensIntent.Toggle("ethereum"))
        advanceUntilIdle()

        coVerify { tokenRepository.setTokenEnabled("w1", "ethereum", true) }
        assertTrue(viewModel.currentState.tokens.first { it.token.id == "ethereum" }.isEnabled)
    }

    @Test
    fun `Toggle disables a currently enabled token`() = runTest(dispatcher) {
        val enabledIds = MutableStateFlow(setOf("bitcoin"))
        every { tokenRepository.observeAllTokens() } returns flowOf(listOf(btc))
        every { tokenRepository.observeEnabledTokenIds("w1") } returns enabledIds
        coEvery { tokenRepository.setTokenEnabled("w1", "bitcoin", false) } coAnswers {
            enabledIds.value = enabledIds.value - "bitcoin"
        }
        val viewModel = ManageTokensViewModel(tokenRepository, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(ManageTokensIntent.Toggle("bitcoin"))
        advanceUntilIdle()

        coVerify { tokenRepository.setTokenEnabled("w1", "bitcoin", false) }
        assertFalse(viewModel.currentState.tokens.first { it.token.id == "bitcoin" }.isEnabled)
    }
}
