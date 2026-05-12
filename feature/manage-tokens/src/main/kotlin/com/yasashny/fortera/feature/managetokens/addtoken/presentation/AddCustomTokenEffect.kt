package com.yasashny.fortera.feature.managetokens.addtoken.presentation

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.ui.text.UiText

sealed interface AddCustomTokenEffect : UiEffect {
    data object NavigateBack : AddCustomTokenEffect
    data class ShowError(val message: UiText) : AddCustomTokenEffect
}
