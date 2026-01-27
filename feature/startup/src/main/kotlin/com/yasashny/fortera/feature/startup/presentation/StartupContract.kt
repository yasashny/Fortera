package com.yasashny.fortera.feature.startup.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object StartupContract {

    data class State(
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object CreateWalletClicked : Intent
        data object ImportWalletClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToCreateWallet : Effect
        data object NavigateToImportWallet : Effect
        data object NavigateToMain : Effect
    }
}