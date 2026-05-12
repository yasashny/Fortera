package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.importwallet.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportWalletViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val walletInteractor: WalletInteractor = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty Content`() {
        val viewModel = ImportWalletViewModel(walletInteractor)

        val state = viewModel.currentState as ImportWalletState.Content
        assertEquals("", state.name)
        assertEquals("", state.seedPhrase)
        assertFalse(state.isLoading)
        assertNull(state.nameError)
        assertNull(state.seedPhraseError)
        assertNull(state.errorMessage)
    }

    @Test
    fun `NameChanged updates name and clears its error`() {
        val viewModel = ImportWalletViewModel(walletInteractor)

        viewModel.onIntent(ImportWalletIntent.NameChanged("My wallet"))

        val state = viewModel.currentState as ImportWalletState.Content
        assertEquals("My wallet", state.name)
        assertNull(state.nameError)
    }

    @Test
    fun `SeedPhraseChanged updates seed and clears its error`() {
        val viewModel = ImportWalletViewModel(walletInteractor)

        viewModel.onIntent(ImportWalletIntent.SeedPhraseChanged("hello"))

        val state = viewModel.currentState as ImportWalletState.Content
        assertEquals("hello", state.seedPhrase)
        assertNull(state.seedPhraseError)
    }

    @Test
    fun `ImportClicked with blank inputs sets both field errors`() = runTest(dispatcher) {
        val viewModel = ImportWalletViewModel(walletInteractor)

        viewModel.onIntent(ImportWalletIntent.ImportClicked)
        advanceUntilIdle()

        val state = viewModel.currentState as ImportWalletState.Content
        assertEquals(UiText.of(R.string.import_wallet_error_name_empty), state.nameError)
        assertEquals(UiText.of(R.string.import_wallet_error_seed_empty), state.seedPhraseError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `ImportClicked with blank seed shows only seed error`() = runTest(dispatcher) {
        val viewModel = ImportWalletViewModel(walletInteractor)
        viewModel.onIntent(ImportWalletIntent.NameChanged("wallet"))

        viewModel.onIntent(ImportWalletIntent.ImportClicked)
        advanceUntilIdle()

        val state = viewModel.currentState as ImportWalletState.Content
        assertNull(state.nameError)
        assertEquals(UiText.of(R.string.import_wallet_error_seed_empty), state.seedPhraseError)
    }

    @Test
    fun `ImportClicked with valid inputs imports and emits NavigateToHome`() =
        runTest(dispatcher) {
            coEvery { walletInteractor.importWallet("My wallet", "seed words") } returns
                Result.success(Wallet("w-1", "My wallet"))

            val viewModel = ImportWalletViewModel(walletInteractor)
            viewModel.onIntent(ImportWalletIntent.NameChanged("My wallet"))
            viewModel.onIntent(ImportWalletIntent.SeedPhraseChanged("seed words"))

            viewModel.onIntent(ImportWalletIntent.ImportClicked)
            advanceUntilIdle()

            coVerify { walletInteractor.importWallet("My wallet", "seed words") }
            assertEquals(ImportWalletEffect.NavigateToHome, viewModel.effect.first())
            val state = viewModel.currentState as ImportWalletState.Content
            assertFalse(state.isLoading)
        }

    @Test
    fun `ImportClicked failure with non-blank message surfaces it as UiText`() =
        runTest(dispatcher) {
            coEvery { walletInteractor.importWallet(any(), any()) } returns
                Result.failure(IllegalArgumentException("Seed phrase contains invalid words"))

            val viewModel = ImportWalletViewModel(walletInteractor)
            viewModel.onIntent(ImportWalletIntent.NameChanged("wallet"))
            viewModel.onIntent(ImportWalletIntent.SeedPhraseChanged("garbage"))

            viewModel.onIntent(ImportWalletIntent.ImportClicked)
            advanceUntilIdle()

            val state = viewModel.currentState as ImportWalletState.Content
            assertEquals(UiText.of("Seed phrase contains invalid words"), state.errorMessage)
            assertFalse(state.isLoading)
        }

    @Test
    fun `ImportClicked failure with blank message falls back to import_failed`() =
        runTest(dispatcher) {
            coEvery { walletInteractor.importWallet(any(), any()) } returns
                Result.failure(RuntimeException(""))

            val viewModel = ImportWalletViewModel(walletInteractor)
            viewModel.onIntent(ImportWalletIntent.NameChanged("wallet"))
            viewModel.onIntent(ImportWalletIntent.SeedPhraseChanged("garbage"))

            viewModel.onIntent(ImportWalletIntent.ImportClicked)
            advanceUntilIdle()

            val state = viewModel.currentState as ImportWalletState.Content
            assertEquals(UiText.of(R.string.import_wallet_error_import_failed), state.errorMessage)
        }

    @Test
    fun `DismissError clears errorMessage`() = runTest(dispatcher) {
        coEvery { walletInteractor.importWallet(any(), any()) } returns
            Result.failure(RuntimeException("oops"))
        val viewModel = ImportWalletViewModel(walletInteractor)
        viewModel.onIntent(ImportWalletIntent.NameChanged("wallet"))
        viewModel.onIntent(ImportWalletIntent.SeedPhraseChanged("garbage"))
        viewModel.onIntent(ImportWalletIntent.ImportClicked)
        advanceUntilIdle()
        assertNotNull((viewModel.currentState as ImportWalletState.Content).errorMessage)

        viewModel.onIntent(ImportWalletIntent.DismissError)

        assertNull((viewModel.currentState as ImportWalletState.Content).errorMessage)
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest(dispatcher) {
        val viewModel = ImportWalletViewModel(walletInteractor)

        viewModel.onIntent(ImportWalletIntent.BackClicked)

        assertEquals(ImportWalletEffect.NavigateBack, viewModel.effect.first())
    }
}
