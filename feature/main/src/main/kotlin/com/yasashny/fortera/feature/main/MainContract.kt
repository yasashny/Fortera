package com.yasashny.fortera.feature.main

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object MainContract {

    data class State(
        val activeWalletName: String? = null,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent

    sealed interface Effect : UiEffect {
        data object NavigateToStartup : Effect
    }
}
