package com.yasashny.fortera.feature.receive.confirmsend

import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

internal object ConfirmSendContract {

    /**
     * [nativeAmount] is already a human-ready string ("0.000150 ETH") — that formatting is
     * symbol-specific, not currency-specific, so it's done in the ViewModel.
     *
     * [feeUsd] is the canonical USD fee, left as a raw `Double`. The Layout picks up the
     * current display currency via [com.yasashny.fortera.core.ui.currency.LocalCurrency] and
     * formats it at render time — that way switching currency updates the fee chip live, without
     * the ViewModel needing to re-compute its strings.
     */
    data class CommissionInfo(
        val estimate: FeeEstimate,
        val nativeAmount: String,
        val feeUsd: Double,
    )

    data class State(
        val tokenName: String = "",
        val tokenSymbol: String = "",
        /** Empty when the wallet name is unknown — the Layout falls back to a localised default. */
        val walletName: String = "",
        val amount: String = "",
        /** Raw USD equivalent of the send amount. Null until prices load. Layout formats. */
        val amountUsd: Double? = null,
        val address: String = "",
        val networkName: String = "",
        val commissions: Map<FeeSpeed, CommissionInfo> = emptyMap(),
        val selectedSpeed: FeeSpeed = FeeSpeed.FAST,
        val totalAmount: String = "",
        /** Raw USD total (send amount + fee, priced at latest rates). Null until fees load. */
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
