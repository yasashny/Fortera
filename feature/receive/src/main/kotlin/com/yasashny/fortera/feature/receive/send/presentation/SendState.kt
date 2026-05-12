package com.yasashny.fortera.feature.receive.send.presentation

import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText
import java.math.BigDecimal

internal data class SendState(
    val tokenId: String = "",
    val tokenName: String = "",
    val tokenSymbol: String = "",
    val tokenIconUrl: String = "",
    val balance: BigDecimal = BigDecimal.ZERO,
    val priceUsd: Double = 0.0,
    val amount: String = "",
    val amountUsd: Double? = null,
    val address: String = "",
    val insufficientFunds: Boolean = false,
    val amountError: UiText? = null,
    val addressError: UiText? = null,
    val isLoading: Boolean = true,
) : UiState {

    val isAmountValid: Boolean
        get() = amount.normalizeDecimal().toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true

    val canContinue: Boolean
        get() = isAmountValid &&
            address.isNotBlank() &&
            !insufficientFunds &&
            amountError == null &&
            addressError == null
}

internal fun String.normalizeDecimal(): String = replace(',', '.')
