package com.yasashny.fortera.feature.main.ui.format

import java.math.BigDecimal
import java.text.DecimalFormat

private val UsdFormatter = DecimalFormat("#,##0.00").apply {
    decimalFormatSymbols = decimalFormatSymbols.apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
}

internal fun formatUsd(amount: Double): String = "$${UsdFormatter.format(amount)}"

internal fun formatCrypto(amount: BigDecimal): String {
    val plain = amount.toPlainString()
    val dotIndex = plain.indexOf('.')
    return if (dotIndex == -1 || plain.length - dotIndex <= 7) plain
    else plain.substring(0, dotIndex + 7)
}
