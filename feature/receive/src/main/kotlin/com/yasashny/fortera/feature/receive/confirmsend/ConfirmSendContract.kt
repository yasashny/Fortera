package com.yasashny.fortera.feature.receive.confirmsend

import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

internal object ConfirmSendContract {

    data class CommissionInfo(
        val nativeAmount: String,
        val usdAmount: String,
    )

    data class State(
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val walletName: String = "",
        val amount: String = "",
        val amountUsd: String = "",
        val address: String = "",
        val networkName: String = "",
        val commissions: Map<FeeSpeed, CommissionInfo> = emptyMap(),
        val selectedSpeed: FeeSpeed = FeeSpeed.FAST,
        val totalAmount: String = "",
        val totalAmountUsd: String = "",
        val isFeesLoading: Boolean = true,
        val isSpeedSheetOpen: Boolean = false,
        val isSending: Boolean = false,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    ) : UiState {
        val commission: CommissionInfo? get() = commissions[selectedSpeed]
    }

    sealed interface Intent : UiIntent {
        data object Send : Intent
        data object OpenSpeedSheet : Intent
        data object DismissSpeedSheet : Intent
        data class SelectSpeed(val speed: FeeSpeed) : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data object NavigateToSuccess : Effect
    }
}
