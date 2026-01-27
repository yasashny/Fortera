package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import kotlinx.coroutines.flow.combine

class WalletSelectorViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<WalletSelectorContract.State, WalletSelectorContract.Intent, WalletSelectorContract.Effect>(
    WalletSelectorContract.State()
) {

    init {
        intent {
            launch {
                combine(
                    walletInteractor.getWallets(),
                    walletInteractor.observeActiveWallet(),
                ) { wallets, activeWallet ->
                    WalletSelectorContract.State(
                        wallets = wallets,
                        activeWalletId = activeWallet?.id,
                        isLoading = false
                    )
                }.collect { reduce(it) }
            }
        }
    }

    override fun handleIntent(intent: WalletSelectorContract.Intent) {
        when (intent) {
            is WalletSelectorContract.Intent.SelectWallet -> selectWallet(intent.id)
            is WalletSelectorContract.Intent.SettingsClicked -> intent {
                sendEffect(
                    WalletSelectorContract.Effect.NavigateToSettings(intent.walletId)
                )
            }

            WalletSelectorContract.Intent.CreateWalletClicked -> intent {
                sendEffect(
                    WalletSelectorContract.Effect.NavigateToCreateWallet
                )
            }

            WalletSelectorContract.Intent.ImportWalletClicked -> intent {
                sendEffect(
                    WalletSelectorContract.Effect.NavigateToImportWallet
                )
            }
        }
    }

    private fun selectWallet(id: String) = intent {
        walletInteractor.setActiveWallet(id)
            .onFailure {
                sendEffect(
                    WalletSelectorContract.Effect.ShowError(
                        it.message ?: "Failed to select wallet"
                    )
                )
            }
    }
}