package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

object WalletSettingsContract {

    sealed interface State : UiState {
        /** Present on both substates — the Screen watches it and shows the shared ErrorDialog. */
        val errorMessage: UiText?

        data object Loading : State {
            override val errorMessage: UiText? = null
        }

        data class Content(
            val walletId: String,
            val name: String,
            val isSaving: Boolean = false,
            val showDeleteDialog: Boolean = false,
            override val errorMessage: UiText? = null,
        ) : State
    }

    sealed interface Intent : UiIntent {
        data class NameChanged(val value: String) : Intent
        data object SaveClicked : Intent
        data object DeleteClicked : Intent
        data object ConfirmDelete : Intent
        data object DismissDeleteDialog : Intent
        data object BackClicked : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSuccess(val message: UiText) : Effect
    }
}
