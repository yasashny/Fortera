package com.yasashny.fortera.feature.startup.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface StartupEffect : UiEffect {
    data object NavigateToCreateWallet : StartupEffect
    data object NavigateToImportWallet : StartupEffect
}
