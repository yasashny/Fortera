package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.ui.text.UiText

sealed interface WalletSettingsEffect : UiEffect {
    data object NavigateBack : WalletSettingsEffect
    data class ShowSuccess(val message: UiText) : WalletSettingsEffect
}
