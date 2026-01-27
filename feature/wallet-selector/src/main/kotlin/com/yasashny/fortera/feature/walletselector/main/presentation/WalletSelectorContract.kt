package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object WalletSelectorContract {

    data class State(
        val wallets: List<Wallet> = emptyList(),
        val activeWalletId: String? = null,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class SelectWallet(val id: String) : Intent
        data class SettingsClicked(val walletId: String) : Intent
        data object CreateWalletClicked : Intent
        data object ImportWalletClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToCreateWallet : Effect
        data object NavigateToImportWallet : Effect
        data class NavigateToSettings(val walletId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}