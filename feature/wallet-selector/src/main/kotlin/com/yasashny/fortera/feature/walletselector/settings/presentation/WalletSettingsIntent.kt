package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface WalletSettingsIntent : UiIntent {
    data class NameChanged(val value: String) : WalletSettingsIntent
    data object SaveClicked : WalletSettingsIntent
    data object DeleteClicked : WalletSettingsIntent
    data object ConfirmDelete : WalletSettingsIntent
    data object DismissDeleteDialog : WalletSettingsIntent
    data object BackClicked : WalletSettingsIntent
    data object DismissError : WalletSettingsIntent
}
