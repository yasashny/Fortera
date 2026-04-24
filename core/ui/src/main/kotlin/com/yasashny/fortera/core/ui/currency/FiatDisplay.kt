package com.yasashny.fortera.core.ui.currency

import com.yasashny.fortera.core.common.currency.Currency

/**
 * Everything a composable needs to render a USD-canonical amount in the user's selected fiat.
 *
 * Bundles the user's picked [currency] (stable, chosen in Settings) with [rateFromUsd] (the
 * live USD→currency multiplier, refreshed periodically by
 * [com.yasashny.fortera.core.network.currency.FiatRateRepository]).
 *
 * [rateFromUsd] is nullable — `null` means the rate hasn't been fetched yet (first launch
 * without a persisted snapshot). The codebase deliberately has no bootstrap or fallback rates:
 * fiat display sites render a shimmer when the rate is missing rather than invent a number.
 * Once rates arrive, the CompositionLocal re-emits and the shimmers resolve into real values.
 *
 * Data-layer models (`PriceInfo`, `TokenBalance`, `WalletBalancesSnapshot`) stay USD-canonical
 * and don't know about this type — conversion is a display concern.
 */
data class FiatDisplay(
    val currency: Currency,
    val rateFromUsd: Double?,
) {
    /** True once we have a live rate and can actually format fiat values. */
    val isReady: Boolean get() = rateFromUsd != null

    companion object {
        /** Selected currency with no rate yet — triggers shimmers at every fiat render site. */
        val Default: FiatDisplay = FiatDisplay(Currency.Default, rateFromUsd = null)
    }
}
