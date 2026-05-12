package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R
import kotlinx.coroutines.flow.combine

class WalletSelectorViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<WalletSelectorState, WalletSelectorIntent, WalletSelectorEffect>(WalletSelectorState()) {

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

    override fun handleIntent(intent: WalletSelectorIntent) = when (intent) {
        is WalletSelectorIntent.SelectWallet -> selectWallet(intent.id)
        is WalletSelectorIntent.SettingsClicked ->
            sendEffect(WalletSelectorEffect.NavigateToSettings(intent.walletId))
        WalletSelectorIntent.CreateWalletClicked ->
            sendEffect(WalletSelectorEffect.NavigateToCreateWallet)
        WalletSelectorIntent.ImportWalletClicked ->
            sendEffect(WalletSelectorEffect.NavigateToImportWallet)
        WalletSelectorIntent.DismissError ->
            updateState { it.copy(errorMessage = null) }
    }

    private fun selectWallet(id: String) {
        intent {
            walletInteractor.setActiveWallet(id).onFailure {
                updateState { it.copy(errorMessage = UiText.of(R.string.wallet_selector_error_select_failed)) }
            }
        }
    }
}
