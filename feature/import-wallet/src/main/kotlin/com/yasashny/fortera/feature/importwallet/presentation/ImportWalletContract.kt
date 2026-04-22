package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

object ImportWalletContract {

    sealed interface State : UiState {
        val errorMessage: UiText?

        data class Content(
            val name: String = "",
            val seedPhrase: String = "",
            val isLoading: Boolean = false,
            val nameError: UiText? = null,
            val seedPhraseError: UiText? = null,
            override val errorMessage: UiText? = null,
        ) : State
    }

    sealed interface Intent : UiIntent {
        data class NameChanged(val value: String) : Intent
        data class SeedPhraseChanged(val value: String) : Intent
        data object ImportClicked : Intent
        data object BackClicked : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data object NavigateToHome : Effect
    }
}
