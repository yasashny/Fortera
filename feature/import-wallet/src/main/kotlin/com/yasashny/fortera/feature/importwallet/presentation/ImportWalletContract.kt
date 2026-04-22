package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object ImportWalletContract {

    sealed interface State : UiState {
        data class Content(
            val name: String = "",
            val seedPhrase: String = "",
            val isLoading: Boolean = false,
            val nameError: String? = null,
            val seedPhraseError: String? = null
        ) : State
    }

    sealed interface Intent : UiIntent {
        data class NameChanged(val value: String) : Intent
        data class SeedPhraseChanged(val value: String) : Intent
        data object ImportClicked : Intent
        data object BackClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data object NavigateToHome : Effect
        data class ShowError(val message: String) : Effect
    }
}
