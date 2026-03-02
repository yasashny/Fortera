package com.yasashny.fortera.feature.main

import com.yasashny.fortera.core.cryptoapi.model.TokenBalance
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object MainContract {

    data class State(
        val activeWalletName: String? = null,
        val totalUsd: Double = 0.0,
        val tokens: List<TokenBalance> = emptyList(),
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object OpenManageTokens : Intent
        data object OpenWalletSelector : Intent
        data object OpenSettings : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToStartup : Effect
        data object NavigateToManageTokens : Effect
        data object NavigateToSettings : Effect
    }
}
