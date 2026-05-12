package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.domain.wallet.SeedPhrase
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateWalletViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val walletInteractor: WalletInteractor = mockk()
    private val sampleSeed = SeedPhrase(
        listOf(
            "abandon", "ability", "able", "about", "above", "absent",
            "absorb", "abstract", "absurd", "abuse", "access", "accident",
        )
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
    fun `initial state is Loading`() {
        coEvery { walletInteractor.generateSeedPhrase() } returns Result.success(sampleSeed)

        val viewModel = CreateWalletViewModel(walletInteractor)

        assertEquals(CreateWalletState.Loading, viewModel.currentState)
    }

    @Test
    fun `init generates seed phrase and renders it`() = runTest(dispatcher) {
        coEvery { walletInteractor.generateSeedPhrase() } returns Result.success(sampleSeed)

        val viewModel = CreateWalletViewModel(walletInteractor)
        advanceUntilIdle()

        val state = viewModel.currentState as CreateWalletState.ShowSeedPhrase
        assertEquals(sampleSeed.words, state.seedPhrase)
        assertFalse(state.isCreating)
    }

    @Test
    fun `generateSeedPhrase failure emits ShowError and NavigateBack`() = runTest(dispatcher) {
        coEvery { walletInteractor.generateSeedPhrase() } returns
            Result.failure(RuntimeException("rng down"))

        val viewModel = CreateWalletViewModel(walletInteractor)
        advanceUntilIdle()

        val showError = viewModel.effect.first()
        assertTrue("Expected ShowError, got $showError", showError is CreateWalletEffect.ShowError)
        assertEquals("rng down", (showError as CreateWalletEffect.ShowError).message)
        assertEquals(CreateWalletEffect.NavigateBack, viewModel.effect.first())
    }

    @Test
    fun `CreateClicked formats name with wallet count and emits NavigateToMain`() =
        runTest(dispatcher) {
            coEvery { walletInteractor.generateSeedPhrase() } returns Result.success(sampleSeed)
            coEvery { walletInteractor.getWalletCount() } returns Result.success(3)
            coEvery { walletInteractor.createWallet(any(), any()) } returns
                Result.success(Wallet("w-4", "Wallet 4"))

            val viewModel = CreateWalletViewModel(walletInteractor)
            advanceUntilIdle()

            viewModel.onIntent(CreateWalletIntent.CreateClicked(nameTemplate = "Wallet %d"))
            advanceUntilIdle()

            coVerify { walletInteractor.createWallet("Wallet 4", sampleSeed) }
            assertEquals(CreateWalletEffect.NavigateToMain, viewModel.effect.first())
        }

    @Test
    fun `CreateClicked falls back to count zero when interactor fails`() = runTest(dispatcher) {
        coEvery { walletInteractor.generateSeedPhrase() } returns Result.success(sampleSeed)
        coEvery { walletInteractor.getWalletCount() } returns Result.failure(RuntimeException())
        coEvery { walletInteractor.createWallet(any(), any()) } returns
            Result.success(Wallet("w-1", "Wallet 1"))

        val viewModel = CreateWalletViewModel(walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(CreateWalletIntent.CreateClicked("Wallet %d"))
        advanceUntilIdle()

        coVerify { walletInteractor.createWallet("Wallet 1", sampleSeed) }
    }

    @Test
    fun `CreateClicked failure clears isCreating and emits ShowError`() = runTest(dispatcher) {
        coEvery { walletInteractor.generateSeedPhrase() } returns Result.success(sampleSeed)
        coEvery { walletInteractor.getWalletCount() } returns Result.success(0)
        coEvery { walletInteractor.createWallet(any(), any()) } returns
            Result.failure(IllegalStateException("boom"))

        val viewModel = CreateWalletViewModel(walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(CreateWalletIntent.CreateClicked("Wallet %d"))
        advanceUntilIdle()

        val state = viewModel.currentState as CreateWalletState.ShowSeedPhrase
        assertFalse(state.isCreating)
        val effect = viewModel.effect.first()
        assertTrue("Expected ShowError, got $effect", effect is CreateWalletEffect.ShowError)
        assertEquals("boom", (effect as CreateWalletEffect.ShowError).message)
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest(dispatcher) {
        coEvery { walletInteractor.generateSeedPhrase() } returns Result.success(sampleSeed)
        val viewModel = CreateWalletViewModel(walletInteractor)
        advanceUntilIdle()

        viewModel.onIntent(CreateWalletIntent.BackClicked)

        assertEquals(CreateWalletEffect.NavigateBack, viewModel.effect.first())
    }
}
