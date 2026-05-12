package com.yasashny.fortera.feature.walletselector.settings.presentation

import androidx.compose.runtime.Immutable
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

@Immutable
sealed interface WalletSettingsState : UiState {
    val errorMessage: UiText?

    data object Loading : WalletSettingsState {
        override val errorMessage: UiText? = null
    }

    data class Content(
        val walletId: String,
        val name: String,
        val isSaving: Boolean = false,
        val showDeleteDialog: Boolean = false,
        override val errorMessage: UiText? = null,
    ) : WalletSettingsState
}
