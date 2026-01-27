package com.yasashny.fortera.feature.main

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object MainContract {

    data class State(
        val wallets: List<Wallet> = emptyList(),
        val activeWalletId: String? = null,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class DeleteWallet(val id: String) : Intent
        data class SelectWallet(val id: String) : Intent
        data object CreateNewWallet : Intent
        data object ImportWallet : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToCreateWallet : Effect
        data object NavigateToImportWallet : Effect
        data class ShowError(val message: String) : Effect
    }
}
