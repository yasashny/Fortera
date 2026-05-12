package com.yasashny.fortera.feature.walletselector.settings.presentation

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

@OptIn(ExperimentalCoroutinesApi::class)
class WalletSettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val walletInteractor: WalletInteractor = mockk()
    private val wallet = Wallet(id = "w1", name = "Wallet 1", createdAt = 0L)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { walletInteractor.getWallets() } returns flowOf(listOf(wallet))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads wallet into Content state`() = runTest(dispatcher) {
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)

        advanceUntilIdle()

        val content = viewModel.currentState as WalletSettingsState.Content
        assertEquals("w1", content.walletId)
        assertEquals("Wallet 1", content.name)
        assertFalse(content.isSaving)
        assertFalse(content.showDeleteDialog)
    }

    @Test
    fun `NameChanged updates name`() = runTest(dispatcher) {
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(WalletSettingsIntent.NameChanged("Renamed"))

        assertEquals("Renamed", (viewModel.currentState as WalletSettingsState.Content).name)
    }

    @Test
    fun `SaveClicked with blank name surfaces error`() = runTest(dispatcher) {
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSettingsIntent.NameChanged("   "))

        viewModel.onIntent(WalletSettingsIntent.SaveClicked)
        advanceUntilIdle()

        val content = viewModel.currentState as WalletSettingsState.Content
        assertEquals(UiText.of(R.string.wallet_settings_error_name_empty), content.errorMessage)
        assertFalse(content.isSaving)
    }

    @Test
    fun `SaveClicked success emits ShowSuccess`() = runTest(dispatcher) {
        coEvery { walletInteractor.updateWalletName("w1", "New name") } returns Result.success(Unit)
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSettingsIntent.NameChanged("New name"))

        viewModel.onIntent(WalletSettingsIntent.SaveClicked)
        advanceUntilIdle()

        coVerify { walletInteractor.updateWalletName("w1", "New name") }
        val effect = viewModel.effect.first()
        assertTrue("Expected ShowSuccess, got $effect", effect is WalletSettingsEffect.ShowSuccess)
        assertFalse((viewModel.currentState as WalletSettingsState.Content).isSaving)
    }

    @Test
    fun `SaveClicked failure surfaces error and clears saving flag`() = runTest(dispatcher) {
        coEvery { walletInteractor.updateWalletName(any(), any()) } returns
            Result.failure(RuntimeException("boom"))
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSettingsIntent.NameChanged("New"))

        viewModel.onIntent(WalletSettingsIntent.SaveClicked)
        advanceUntilIdle()

        val content = viewModel.currentState as WalletSettingsState.Content
        assertFalse(content.isSaving)
        assertEquals(UiText.of(R.string.wallet_settings_error_save_failed), content.errorMessage)
    }

    @Test
    fun `DeleteClicked opens dialog`() = runTest(dispatcher) {
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(WalletSettingsIntent.DeleteClicked)

        assertTrue((viewModel.currentState as WalletSettingsState.Content).showDeleteDialog)
    }

    @Test
    fun `DismissDeleteDialog closes dialog`() = runTest(dispatcher) {
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSettingsIntent.DeleteClicked)

        viewModel.onIntent(WalletSettingsIntent.DismissDeleteDialog)

        assertFalse((viewModel.currentState as WalletSettingsState.Content).showDeleteDialog)
    }

    @Test
    fun `ConfirmDelete deletes wallet and navigates back`() = runTest(dispatcher) {
        coEvery { walletInteractor.deleteWallet("w1") } returns Result.success(Unit)
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSettingsIntent.DeleteClicked)

        viewModel.onIntent(WalletSettingsIntent.ConfirmDelete)
        advanceUntilIdle()

        coVerify { walletInteractor.deleteWallet("w1") }
        assertEquals(WalletSettingsEffect.NavigateBack, viewModel.effect.first())
    }

    @Test
    fun `ConfirmDelete failure surfaces error`() = runTest(dispatcher) {
        coEvery { walletInteractor.deleteWallet(any()) } returns
            Result.failure(RuntimeException("locked"))
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(WalletSettingsIntent.ConfirmDelete)
        advanceUntilIdle()

        val content = viewModel.currentState as WalletSettingsState.Content
        assertEquals(UiText.of(R.string.wallet_settings_error_delete_failed), content.errorMessage)
        assertFalse(content.isSaving)
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest(dispatcher) {
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(WalletSettingsIntent.BackClicked)

        assertEquals(WalletSettingsEffect.NavigateBack, viewModel.effect.first())
    }

    @Test
    fun `DismissError clears error`() = runTest(dispatcher) {
        coEvery { walletInteractor.updateWalletName(any(), any()) } returns
            Result.failure(RuntimeException("boom"))
        val viewModel = WalletSettingsViewModel(wallet.id, walletInteractor)
        advanceUntilIdle()
        viewModel.onIntent(WalletSettingsIntent.NameChanged("New"))
        viewModel.onIntent(WalletSettingsIntent.SaveClicked)
        advanceUntilIdle()

        viewModel.onIntent(WalletSettingsIntent.DismissError)

        assertNull((viewModel.currentState as WalletSettingsState.Content).errorMessage)
    }
}
