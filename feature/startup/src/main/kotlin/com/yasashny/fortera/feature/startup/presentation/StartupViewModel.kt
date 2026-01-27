package com.yasashny.fortera.feature.startup.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import kotlinx.coroutines.flow.first

class StartupViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<StartupContract.State, StartupContract.Intent, StartupContract.Effect>(
    StartupContract.State()
) {
    init {
        intent {
            val activeWallet = walletInteractor.observeActiveWallet().first()
            if (activeWallet != null) {
                sendEffect(StartupContract.Effect.NavigateToMain)
            } else {
                reduce(StartupContract.State(isLoading = false))
            }
        }
    }

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