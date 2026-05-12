package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface ImportWalletEffect : UiEffect {
    data object NavigateBack : ImportWalletEffect
    data object NavigateToHome : ImportWalletEffect
}
