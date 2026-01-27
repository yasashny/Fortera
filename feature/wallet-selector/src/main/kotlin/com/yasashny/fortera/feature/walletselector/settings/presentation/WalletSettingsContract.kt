package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object WalletSettingsContract {

    sealed interface State : UiState {
        data object Loading : State
        data class Content(
            val walletId: String,
            val name: String,
            val isSaving: Boolean = false,
            val showDeleteDialog: Boolean = false,
        ) : State
    }

    sealed interface Intent : UiIntent {
        data class NameChanged(val value: String) : Intent
        data object SaveClicked : Intent
        data object DeleteClicked : Intent
        data object ConfirmDelete : Intent
        data object DismissDeleteDialog : Intent
        data object BackClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
        data class ShowSuccess(val message: String) : Effect
    }
}