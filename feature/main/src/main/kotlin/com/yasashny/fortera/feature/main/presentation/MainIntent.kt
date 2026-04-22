package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface MainIntent : UiIntent {

    data object Refresh : MainIntent

    data object OpenWalletSelector : MainIntent
    data object DismissWalletSelector : MainIntent

    data object DismissBanner : MainIntent

    data object OpenSettings : MainIntent
    data object OpenSend : MainIntent
    data object OpenReceive : MainIntent
    data object OpenManageTokens : MainIntent
    data class OpenTokenDetails(val tokenId: String) : MainIntent
}
