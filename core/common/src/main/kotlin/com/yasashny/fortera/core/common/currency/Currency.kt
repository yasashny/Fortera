package com.yasashny.fortera.core.common.currency

/**
 * Fiat currencies the wallet can display values in.
 *
 * Internal balances and prices are always stored in USD (the CoinStats canonical unit).
 * Display-time conversion uses live USD→currency rates sourced from
 * [com.yasashny.fortera.core.network.currency.FiatRateRepository] and bundled with this enum
 * into a [com.yasashny.fortera.core.ui.currency.FiatDisplay] at the UI layer. This enum
 * therefore only carries presentation-time metadata — the symbol, where to place it, and the
 * stable code used as a cache/API key. Rates deliberately live outside to keep the display
 * currency independent of rate freshness.
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val symbolPosition: CurrencySymbolPosition,
) {
    USD(code = "USD", symbol = "$", symbolPosition = CurrencySymbolPosition.PREFIX),
    EUR(code = "EUR", symbol = "€", symbolPosition = CurrencySymbolPosition.PREFIX),
    RUB(code = "RUB", symbol = "₽", symbolPosition = CurrencySymbolPosition.SUFFIX),
    CNY(code = "CNY", symbol = "¥", symbolPosition = CurrencySymbolPosition.PREFIX),
    ;

    companion object {
        val Default: Currency = USD

        fun fromCodeOrDefault(code: String?): Currency =
            entries.firstOrNull { it.code == code } ?: Default
    }
}

enum class CurrencySymbolPosition { PREFIX, SUFFIX }
