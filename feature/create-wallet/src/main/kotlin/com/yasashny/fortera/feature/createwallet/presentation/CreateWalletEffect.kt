package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface CreateWalletEffect : UiEffect {
    data object NavigateBack : CreateWalletEffect
    data object NavigateToMain : CreateWalletEffect
    data class ShowError(val message: String) : CreateWalletEffect
}
