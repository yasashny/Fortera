package com.yasashny.fortera.feature.managetokens.addtoken.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface AddCustomTokenIntent : UiIntent {
    data class InputChanged(val value: String) : AddCustomTokenIntent
    data object AddClicked : AddCustomTokenIntent
}
