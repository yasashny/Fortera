package com.yasashny.fortera.feature.receive.confirmsend.presentation

import com.yasashny.fortera.core.mvi.UiEffect

internal sealed interface ConfirmSendEffect : UiEffect {
    data object NavigateBack : ConfirmSendEffect
    data class NavigateToSuccess(val amount: String, val symbol: String) : ConfirmSendEffect
}
