package com.yasashny.fortera.feature.receive.confirmsend.presentation

import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

internal data class ConfirmSendState(
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

internal data class CommissionInfo(
    val estimate: FeeEstimate,
    val nativeAmount: String,
    val feeUsd: Double,
)
