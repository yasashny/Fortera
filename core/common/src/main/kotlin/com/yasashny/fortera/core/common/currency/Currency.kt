package com.yasashny.fortera.core.common.currency

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
