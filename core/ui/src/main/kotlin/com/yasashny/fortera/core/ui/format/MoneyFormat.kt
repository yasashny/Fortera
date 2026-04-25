package com.yasashny.fortera.core.ui.format

import com.yasashny.fortera.core.common.currency.CurrencySymbolPosition
import com.yasashny.fortera.core.ui.currency.FiatDisplay
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private val FiatFormatter = DecimalFormat("#,##0.00").apply {
    decimalFormatSymbols = DecimalFormatSymbols().apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
}

fun formatFiat(
    amountUsd: Double,
    fiat: FiatDisplay,
    approximate: Boolean = false,
): String? {
    val rate = fiat.rateFromUsd ?: return null
    val converted = amountUsd * rate
    val formattedNumber = FiatFormatter.format(converted)
    val body = when (fiat.currency.symbolPosition) {
        CurrencySymbolPosition.PREFIX -> "${fiat.currency.symbol}$formattedNumber"
        CurrencySymbolPosition.SUFFIX -> "$formattedNumber ${fiat.currency.symbol}"
    }
    return if (approximate) "≈ $body" else body
}

fun formatCrypto(amount: BigDecimal): String {
    val plain = amount.toPlainString()
    val dotIndex = plain.indexOf('.')
    return if (dotIndex == -1 || plain.length - dotIndex <= 7) plain
    else plain.substring(0, dotIndex + 7)
}

fun formatCrypto(amount: Double): String = formatCrypto(BigDecimal.valueOf(amount))
