package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface MainEffect : UiEffect {
    data object NavigateToStartup : MainEffect
    data object NavigateToSettings : MainEffect
    data object NavigateToSend : MainEffect
    data object NavigateToReceive : MainEffect
    data object NavigateToManageTokens : MainEffect
    data class NavigateToTokenDetails(val tokenId: String) : MainEffect
}
