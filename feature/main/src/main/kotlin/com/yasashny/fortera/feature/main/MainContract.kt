package com.yasashny.fortera.feature.main

import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object MainContract {

    data class State(
        val activeWalletName: String? = null,
        val totalUsd: Double = 0.0,
        val tokens: List<TokenBalance> = emptyList(),
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Refresh : Intent
        data object OpenManageTokens : Intent
        data object OpenWalletSelector : Intent
        data object OpenSettings : Intent
        data class OpenTokenDetails(val tokenId: String) : Intent
        data object OpenReceive : Intent
        data object OpenSend : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToStartup : Effect
        data object NavigateToManageTokens : Effect
        data object NavigateToSettings : Effect
        data class NavigateToTokenDetails(val tokenId: String) : Effect
        data object NavigateToSelectTokenForReceive : Effect
        data object NavigateToSelectTokenForSend : Effect
    }
}
