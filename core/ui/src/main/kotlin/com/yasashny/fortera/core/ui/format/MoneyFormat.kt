package com.yasashny.fortera.core.ui.format

import java.math.BigDecimal
import java.text.DecimalFormat

private val UsdFormatter = DecimalFormat("#,##0.00").apply {
    decimalFormatSymbols = decimalFormatSymbols.apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
}

/** `$1 234,56` — USD with space grouping and comma decimals. */
fun formatUsd(amount: Double): String = "$${UsdFormatter.format(amount)}"

/**
 * Crypto amount trimmed to 6 fractional digits. Keeps the full integer part, avoids scientific notation.
 */
fun formatCrypto(amount: BigDecimal): String {
    val plain = amount.toPlainString()
    val dotIndex = plain.indexOf('.')
    return if (dotIndex == -1 || plain.length - dotIndex <= 7) plain
    else plain.substring(0, dotIndex + 7)
}

/** Convenience — [formatCrypto] for `Double` sources (non-wallet-scale values). */
fun formatCrypto(amount: Double): String = formatCrypto(BigDecimal.valueOf(amount))
