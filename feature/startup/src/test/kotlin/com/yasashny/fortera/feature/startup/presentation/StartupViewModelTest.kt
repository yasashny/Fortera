package com.yasashny.fortera.feature.startup.presentation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StartupViewModelTest {

    @Test
    fun `initial state is StartupState`() {
        val viewModel = StartupViewModel()

        assertEquals(StartupState, viewModel.currentState)
    }

    @Test
    fun `CreateWalletClicked emits NavigateToCreateWallet`() = runTest {
        val viewModel = StartupViewModel()

        viewModel.onIntent(StartupIntent.CreateWalletClicked)

        assertEquals(StartupEffect.NavigateToCreateWallet, viewModel.effect.first())
    }

    @Test
    fun `ImportWalletClicked emits NavigateToImportWallet`() = runTest {
        val viewModel = StartupViewModel()

        viewModel.onIntent(StartupIntent.ImportWalletClicked)

        assertEquals(StartupEffect.NavigateToImportWallet, viewModel.effect.first())
    }
}
