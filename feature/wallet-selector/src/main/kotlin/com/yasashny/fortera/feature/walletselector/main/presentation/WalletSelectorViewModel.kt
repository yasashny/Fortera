package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract.Effect
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract.Intent
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract.State
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract.WalletsState
import kotlinx.coroutines.flow.combine

class WalletSelectorViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            combine(
                walletInteractor.getWallets(),
                walletInteractor.observeActiveWallet(),
            ) { wallets, active ->
                WalletsState.Ready(items = wallets, activeWalletId = active?.id)
            }.collect { ready ->
                updateState { it.copy(wallets = ready) }
            }
        }
    }

    override fun handleIntent(intent: Intent) = when (intent) {
        is Intent.SelectWallet -> selectWallet(intent.id)
        is Intent.SettingsClicked -> sendEffect(Effect.NavigateToSettings(intent.walletId))
        Intent.CreateWalletClicked -> sendEffect(Effect.NavigateToCreateWallet)
        Intent.ImportWalletClicked -> sendEffect(Effect.NavigateToImportWallet)
        Intent.DismissError -> updateState { it.copy(errorMessage = null) }
    }

    private fun selectWallet(id: String) {
        intent {
            walletInteractor.setActiveWallet(id).onFailure {
                updateState { it.copy(errorMessage = UiText.of(R.string.wallet_selector_error_select_failed)) }
            }
        }
    }
}
