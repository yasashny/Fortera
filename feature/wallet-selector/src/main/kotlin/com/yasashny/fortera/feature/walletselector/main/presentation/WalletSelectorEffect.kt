package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface WalletSelectorEffect : UiEffect {
    data object NavigateToCreateWallet : WalletSelectorEffect
    data object NavigateToImportWallet : WalletSelectorEffect
    data class NavigateToSettings(val walletId: String) : WalletSelectorEffect
}
