package com.yasashny.fortera.feature.receive.send

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import java.math.BigDecimal

internal object SendContract {

    data class State(
        val tokenId: String = "",
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val balance: BigDecimal = BigDecimal.ZERO,
        val priceUsd: Double = 0.0,
        val amount: String = "",
        val amountUsd: String = "",
        val address: String = "",
        val insufficientFunds: Boolean = false,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class UpdateAmount(val amount: String) : Intent
        data class UpdateAddress(val address: String) : Intent
        data object Continue : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToConfirm(
            val tokenId: String,
            val amount: String,
            val address: String,
        ) : Effect
    }
}
