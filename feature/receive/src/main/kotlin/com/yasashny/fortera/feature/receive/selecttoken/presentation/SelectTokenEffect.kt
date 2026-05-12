package com.yasashny.fortera.feature.receive.selecttoken.presentation

import com.yasashny.fortera.core.mvi.UiEffect

internal sealed interface SelectTokenEffect : UiEffect {
    data class NavigateToReceive(val tokenId: String) : SelectTokenEffect
}
