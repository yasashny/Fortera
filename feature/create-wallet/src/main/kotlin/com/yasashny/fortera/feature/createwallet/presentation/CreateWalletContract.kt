package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object CreateWalletContract {

    sealed interface State : UiState {
        data object Loading : State

        data class ShowSeedPhrase(
            val seedPhrase: List<String>,
            val isCreating: Boolean = false
        ) : State
    }

    sealed interface Intent : UiIntent {
        data object GenerateWallet : Intent
        data class CreateClicked(val nameTemplate: String) : Intent
        data object BackClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data object NavigateToMain : Effect
        data class ShowError(val message: String) : Effect
    }
}
