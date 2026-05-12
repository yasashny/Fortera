package com.yasashny.fortera.feature.receive.send.presentation

import com.yasashny.fortera.core.mvi.UiEffect

internal sealed interface SendEffect : UiEffect {
    data object NavigateBack : SendEffect
    data class NavigateToConfirm(
        val tokenId: String,
        val amount: String,
        val address: String,
    ) : SendEffect
}
