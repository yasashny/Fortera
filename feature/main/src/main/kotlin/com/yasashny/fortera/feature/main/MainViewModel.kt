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
                    Pair(wallets, activeWallet)
                }.collect { (wallets, activeWallet) ->
                    if (wallets.isEmpty()) {
                        sendEffect(Effect.NavigateToStartup)
                    } else {
                        reduce(State(activeWalletName = activeWallet?.name, isLoading = false))
                    }
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) = Unit
}
