package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletSelectorViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val walletInteractor: WalletInteractor = mockk()

    private val walletA = Wallet(id = "a", name = "A", createdAt = 0L)
    private val walletB = Wallet(id = "b", name = "B", createdAt = 0L)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state becomes Ready with wallets and active id`() = runTest(dispatcher) {
        every { walletInteractor.getWallets() } returns flowOf(listOf(walletA, walletB))
        every { walletInteractor.observeActiveWallet() } returns flowOf(walletB)

        val viewModel = WalletSelectorViewModel(walletInteractor)
        advanceUntilIdle()

        val ready = viewModel.currentState.wallets as WalletsState.Ready
        assertEquals(listOf(walletA, walletB), ready.items)
        assertEquals("b", ready.activeWalletId)
    }

    @Test
    fun `SelectWallet success updates the active wallet via interactor`() = runTest(dispatcher) {
        val active = MutableStateFlow<Wallet?>(walletA)
        every { walletInteractor.getWallets() } returns flowOf(listOf(walletA, walletB))
        every { walletInteractor.observeActiveWallet() } returns active
        coEvery { walletInteractor.setActiveWallet("b") } coAnswers {
            active.value = walletB
            Result.success(Unit)
        }
        val viewModel = WalletSelectorViewModel(walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(WalletSelectorIntent.SelectWallet("b"))
        advanceUntilIdle()

        coVerify { walletInteractor.setActiveWallet("b") }
        val ready = viewModel.currentState.wallets as WalletsState.Ready
        assertEquals("b", ready.activeWalletId)
        assertNull(viewModel.currentState.errorMessage)
    }

    @Test
    fun `SelectWallet failure sets error message`() = runTest(dispatcher) {
        every { walletInteractor.getWallets() } returns flowOf(listOf(walletA))
        every { walletInteractor.observeActiveWallet() } returns flowOf(walletA)
        coEvery { walletInteractor.setActiveWallet(any()) } returns
            Result.failure(RuntimeException("nope"))
        val viewModel = WalletSelectorViewModel(walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(WalletSelectorIntent.SelectWallet("a"))
        advanceUntilIdle()

        assertEquals(
            UiText.of(R.string.wallet_selector_error_select_failed),
            viewModel.currentState.errorMessage,
        )
    }

    @Test
    fun `DismissError clears error message`() = runTest(dispatcher) {
        every { walletInteractor.getWallets() } returns flowOf(listOf(walletA))
        every { walletInteractor.observeActiveWallet() } returns flowOf(walletA)
        coEvery { walletInteractor.setActiveWallet(any()) } returns
            Result.failure(RuntimeException("nope"))
        val viewModel = WalletSelectorViewModel(walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSelectorIntent.SelectWallet("a"))
        advanceUntilIdle()
        assertNotNull(viewModel.currentState.errorMessage)

        viewModel.onIntent(WalletSelectorIntent.DismissError)

        assertNull(viewModel.currentState.errorMessage)
    }

    @Test
    fun `CreateWalletClicked emits NavigateToCreateWallet`() = runTest(dispatcher) {
        every { walletInteractor.getWallets() } returns flowOf(emptyList())
        every { walletInteractor.observeActiveWallet() } returns flowOf(null)
        val viewModel = WalletSelectorViewModel(walletInteractor)

        viewModel.onIntent(WalletSelectorIntent.CreateWalletClicked)

        assertEquals(WalletSelectorEffect.NavigateToCreateWallet, viewModel.effect.first())
    }

    @Test
    fun `ImportWalletClicked emits NavigateToImportWallet`() = runTest(dispatcher) {
        every { walletInteractor.getWallets() } returns flowOf(emptyList())
        every { walletInteractor.observeActiveWallet() } returns flowOf(null)
        val viewModel = WalletSelectorViewModel(walletInteractor)

        viewModel.onIntent(WalletSelectorIntent.ImportWalletClicked)

        assertEquals(WalletSelectorEffect.NavigateToImportWallet, viewModel.effect.first())
    }

    @Test
    fun `SettingsClicked emits NavigateToSettings with id`() = runTest(dispatcher) {
        every { walletInteractor.getWallets() } returns flowOf(emptyList())
        every { walletInteractor.observeActiveWallet() } returns flowOf(null)
        val viewModel = WalletSelectorViewModel(walletInteractor)

        viewModel.onIntent(WalletSelectorIntent.SettingsClicked("a"))

        assertEquals(WalletSelectorEffect.NavigateToSettings("a"), viewModel.effect.first())
    }
}
