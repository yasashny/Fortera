package com.yasashny.fortera.core.ui.format

import com.yasashny.fortera.core.common.currency.CurrencySymbolPosition
import com.yasashny.fortera.core.ui.currency.FiatDisplay
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10

private val FiatSymbols = DecimalFormatSymbols().apply {
    groupingSeparator = ' '
    decimalSeparator = ','
}

fun formatFiat(
    amountUsd: Double,
    fiat: FiatDisplay,
    approximate: Boolean = false,
): String? {
    val rate = fiat.rateFromUsd ?: return null
    val converted = amountUsd * rate
    val formattedNumber = fiatFormatter(fractionDigitsFor(converted)).format(converted)
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

private fun fractionDigitsFor(value: Double): Int {
    val magnitude = abs(value)
    if (magnitude == 0.0 || magnitude >= 0.01) return 2
    val leadingZeros = -floor(log10(magnitude)).toInt() - 1
    return (leadingZeros + 4).coerceIn(2, 10)
}

private fun fiatFormatter(maxFractionDigits: Int): DecimalFormat =
    DecimalFormat("#,##0.00", FiatSymbols).apply {
        maximumFractionDigits = maxFractionDigits
    }
