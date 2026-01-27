package com.yasashny.fortera.feature.main

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.main.MainContract.Effect
import com.yasashny.fortera.feature.main.MainContract.Intent
import com.yasashny.fortera.feature.main.MainContract.State
import kotlinx.coroutines.flow.combine

class MainViewModel(
    private val walletInteractor: WalletInteractor
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            launch {
                combine(
                    walletInteractor.getWallets(),
                    walletInteractor.observeActiveWallet(),
                ) { wallets, activeWallet ->
                    State(wallets = wallets, activeWalletId = activeWallet?.id, isLoading = false)
                }.collect { reduce(it) }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.DeleteWallet -> deleteWallet(intent.id)
            is Intent.SelectWallet -> selectWallet(intent.id)
            Intent.CreateNewWallet -> intent { sendEffect(Effect.NavigateToCreateWallet) }
            Intent.ImportWallet -> intent { sendEffect(Effect.NavigateToImportWallet) }
        }
    }

    private fun deleteWallet(id: String) = intent {
        walletInteractor.deleteWallet(id)
            .onFailure { sendEffect(Effect.ShowError(it.message ?: "Failed to delete wallet")) }
    }

    private fun selectWallet(id: String) = intent {
        walletInteractor.setActiveWallet(id)
            .onFailure { sendEffect(Effect.ShowError(it.message ?: "Failed to select wallet")) }
    }
}
