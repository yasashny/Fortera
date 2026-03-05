package com.yasashny.fortera.feature.receive.receive

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

internal object ReceiveContract {

    data class State(
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val networkName: String = "",
        val networkIconUrl: String? = null,
        val address: String = "",
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
    }
}
