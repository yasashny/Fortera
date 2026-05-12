package com.yasashny.fortera.feature.receive.receive.presentation

import com.yasashny.fortera.core.mvi.UiEffect

internal sealed interface ReceiveEffect : UiEffect {
    data object NavigateBack : ReceiveEffect
}
