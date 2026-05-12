package com.yasashny.fortera.feature.tokendetails.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface TokenDetailsEffect : UiEffect {
    data object NavigateBack : TokenDetailsEffect
    data class NavigateToReceive(val tokenId: String) : TokenDetailsEffect
    data class NavigateToSend(val tokenId: String) : TokenDetailsEffect
}
