package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface WalletSelectorIntent : UiIntent {
    data class SelectWallet(val id: String) : WalletSelectorIntent
    data class SettingsClicked(val walletId: String) : WalletSelectorIntent
    data object CreateWalletClicked : WalletSelectorIntent
    data object ImportWalletClicked : WalletSelectorIntent
    data object DismissError : WalletSelectorIntent
}
