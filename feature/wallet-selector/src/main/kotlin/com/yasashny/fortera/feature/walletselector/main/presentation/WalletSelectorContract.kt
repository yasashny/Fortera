package com.yasashny.fortera.feature.walletselector.main.presentation

import androidx.compose.runtime.Immutable
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

object WalletSelectorContract {

    @Immutable
    data class State(
        val wallets: WalletsState = WalletsState.Loading,
        val errorMessage: UiText? = null,
    ) : UiState

    @Immutable
    sealed interface WalletsState {
        data object Loading : WalletsState
        data class Ready(
            val items: List<Wallet>,
            val activeWalletId: String?,
        ) : WalletsState
    }

    sealed interface Intent : UiIntent {
        data class SelectWallet(val id: String) : Intent
        data class SettingsClicked(val walletId: String) : Intent
        data object CreateWalletClicked : Intent
        data object ImportWalletClicked : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToCreateWallet : Effect
        data object NavigateToImportWallet : Effect
        data class NavigateToSettings(val walletId: String) : Effect
    }
}
