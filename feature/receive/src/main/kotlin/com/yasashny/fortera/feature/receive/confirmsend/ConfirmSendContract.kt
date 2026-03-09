package com.yasashny.fortera.feature.receive.confirmsend

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

internal object ConfirmSendContract {

    data class State(
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val amount: String = "",
        val amountUsd: String = "",
        val address: String = "",
        val networkName: String = "",
        val commission: String = "",
        val isSending: Boolean = false,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Send : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data object NavigateToSuccess : Effect
    }
}
