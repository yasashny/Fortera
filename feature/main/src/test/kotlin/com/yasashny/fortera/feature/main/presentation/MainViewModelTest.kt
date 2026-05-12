package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.feature.main.domain.MainOverview
import com.yasashny.fortera.feature.main.domain.MainOverviewEvent
import com.yasashny.fortera.feature.main.domain.MainOverviewInteractor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val overviewInteractor: MainOverviewInteractor = mockk()
    private val walletInteractor: WalletInteractor = mockk()

    private val activeWallet = Wallet(id = "w1", name = "Primary", createdAt = 0L)

    private val sampleBalances = listOf(
        TokenBalance(
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
            changePercent24h = 1.0,
        ),
    )

    private val sampleOverview = MainOverview(
        walletId = "w1",
        balances = sampleBalances,
        totalUsd = 25.0,
        unreachableNetworks = emptySet(),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns MutableStateFlow(null)

        val viewModel = MainViewModel(overviewInteractor, walletInteractor)

        assertEquals(MainState.Initial, viewModel.currentState)
    }

    @Test
    fun `NoWallets event emits NavigateToStartup`() = runTest(dispatcher) {
        every { overviewInteractor.observe() } returns flowOf(MainOverviewEvent.NoWallets)
        every { walletInteractor.observeActiveWallet() } returns flowOf(null)

        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()

        assertEquals(MainEffect.NavigateToStartup, viewModel.effect.first())
    }

    @Test
    fun `active wallet name flows into state`() = runTest(dispatcher) {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)

        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()

        assertEquals("Primary", viewModel.currentState.walletName)
    }

    @Test
    fun `Data event with fresh overview produces Ready state`() = runTest(dispatcher) {
        val events = MutableSharedFlow<MainOverviewEvent>(extraBufferCapacity = 16)
        every { overviewInteractor.observe() } returns events
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)

        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()
        events.emit(MainOverviewEvent.Data(overview = sampleOverview, isCached = false))
        advanceUntilIdle()

        val ready = viewModel.currentState.balances as BalancesState.Ready
        assertEquals(25.0, ready.totalUsd, 0.0)
        assertFalse(ready.isStale)
        assertNull(viewModel.currentState.banner)
    }

    @Test
    fun `Failed event surfaces GenericError banner`() = runTest(dispatcher) {
        val events = MutableSharedFlow<MainOverviewEvent>(extraBufferCapacity = 16)
        every { overviewInteractor.observe() } returns events
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)

        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()
        events.emit(MainOverviewEvent.Failed(walletId = "w1", cause = RuntimeException()))
        advanceUntilIdle()

        assertEquals(Banner.GenericError, viewModel.currentState.banner)
    }

    @Test
    fun `OpenWalletSelector toggles visibility on`() {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns MutableStateFlow(null)
        val viewModel = MainViewModel(overviewInteractor, walletInteractor)

        viewModel.onIntent(MainIntent.OpenWalletSelector)

        assertTrue(viewModel.currentState.isWalletSelectorVisible)
    }

    @Test
    fun `DismissWalletSelector hides selector`() {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns MutableStateFlow(null)
        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        viewModel.onIntent(MainIntent.OpenWalletSelector)

        viewModel.onIntent(MainIntent.DismissWalletSelector)

        assertFalse(viewModel.currentState.isWalletSelectorVisible)
    }

    @Test
    fun `DismissBanner clears banner`() = runTest(dispatcher) {
        val events = MutableSharedFlow<MainOverviewEvent>(extraBufferCapacity = 16)
        every { overviewInteractor.observe() } returns events
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)
        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()
        events.emit(MainOverviewEvent.Failed("w1", RuntimeException()))
        advanceUntilIdle()

        viewModel.onIntent(MainIntent.DismissBanner)

        assertNull(viewModel.currentState.banner)
    }

    @Test
    fun `Refresh success clears refreshing flag and updates balances`() = runTest(dispatcher) {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)
        coEvery { overviewInteractor.refresh() } returns Result.success(sampleOverview)
        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(MainIntent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.currentState.isRefreshing)
        val ready = viewModel.currentState.balances as BalancesState.Ready
        assertEquals(25.0, ready.totalUsd, 0.0)
    }

    @Test
    fun `Refresh failure sets GenericError banner`() = runTest(dispatcher) {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)
        coEvery { overviewInteractor.refresh() } returns Result.failure(RuntimeException("offline"))
        val viewModel = MainViewModel(overviewInteractor, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(MainIntent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.currentState.isRefreshing)
        assertEquals(Banner.GenericError, viewModel.currentState.banner)
    }

    @Test
    fun `navigation intents emit corresponding effects`() = runTest(dispatcher) {
        every { overviewInteractor.observe() } returns MutableSharedFlow()
        every { walletInteractor.observeActiveWallet() } returns MutableStateFlow(null)
        val viewModel = MainViewModel(overviewInteractor, walletInteractor)

        viewModel.onIntent(MainIntent.OpenSettings)
        assertEquals(MainEffect.NavigateToSettings, viewModel.effect.first())

        viewModel.onIntent(MainIntent.OpenSend)
        assertEquals(MainEffect.NavigateToSend, viewModel.effect.first())

        viewModel.onIntent(MainIntent.OpenReceive)
        assertEquals(MainEffect.NavigateToReceive, viewModel.effect.first())

        viewModel.onIntent(MainIntent.OpenManageTokens)
        assertEquals(MainEffect.NavigateToManageTokens, viewModel.effect.first())

        viewModel.onIntent(MainIntent.OpenTokenDetails("usdc"))
        assertEquals(MainEffect.NavigateToTokenDetails("usdc"), viewModel.effect.first())
    }
}
