package com.yasashny.fortera.feature.startup.presentation

import com.yasashny.fortera.core.mvi.MviViewModel

class StartupViewModel : MviViewModel<StartupContract.State, StartupContract.Intent, StartupContract.Effect>(
    StartupContract.State
) {
    override fun handleIntent(intent: StartupContract.Intent) {
        when (intent) {
            StartupContract.Intent.CreateWalletClicked -> onCreateWalletClicked()
            StartupContract.Intent.ImportWalletClicked -> onImportWalletClicked()
        }
    }

    private fun onCreateWalletClicked() = intent {
        sendEffect(StartupContract.Effect.NavigateToCreateWallet)
    }

    private fun onImportWalletClicked() = intent {
        sendEffect(StartupContract.Effect.NavigateToImportWallet)
    }
}
