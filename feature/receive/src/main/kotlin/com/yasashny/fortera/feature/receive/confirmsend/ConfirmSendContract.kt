package com.yasashny.fortera.feature.receive.confirmsend

import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

internal object ConfirmSendContract {

    data class CommissionInfo(
        val estimate: FeeEstimate,
        val nativeAmount: String,
        val feeUsd: Double,
    )

    data class State(
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val tokenIconUrl: String = "",
        val walletName: String = "",
        val amount: String = "",
        val amountUsd: Double? = null,
        val address: String = "",
        val networkName: String = "",
        val commissions: Map<FeeSpeed, CommissionInfo> = emptyMap(),
        val selectedSpeed: FeeSpeed = FeeSpeed.FAST,
        val totalAmount: String = "",
        val totalAmountUsd: Double? = null,
        val isFeesLoading: Boolean = true,
        val isSpeedSheetOpen: Boolean = false,
        val isAddressSheetOpen: Boolean = false,
        val isSending: Boolean = false,
        val isLoading: Boolean = true,
        val insufficientGas: Boolean = false,
        val errorMessage: UiText? = null,
    ) : UiState {
        val commission: CommissionInfo? get() = commissions[selectedSpeed]
        val canSend: Boolean get() = !isSending && !insufficientGas && commission != null
    }

    sealed interface Intent : UiIntent {
        data object Send : Intent
        data object OpenSpeedSheet : Intent
        data object DismissSpeedSheet : Intent
        data object OpenAddressSheet : Intent
        data object DismissAddressSheet : Intent
        data class SelectSpeed(val speed: FeeSpeed) : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToSuccess(val amount: String, val symbol: String) : Effect
    }
}
