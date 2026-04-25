package com.yasashny.fortera.core.ui.currency

import com.yasashny.fortera.core.common.currency.Currency

data class FiatDisplay(
    val currency: Currency,
    val rateFromUsd: Double?,
) {
    val isReady: Boolean get() = rateFromUsd != null

    companion object {
        val Default: FiatDisplay = FiatDisplay(Currency.Default, rateFromUsd = null)
    }
}
