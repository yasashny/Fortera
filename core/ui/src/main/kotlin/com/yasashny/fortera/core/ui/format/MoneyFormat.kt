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

/**
 * Formats a USD-canonical amount for display in [fiat]'s currency using its live rate.
 *
 * Returns `null` when [fiat] has no rate yet — the caller must render a loading placeholder
 * (typically a [com.yasashny.fortera.core.ui.component.ShimmerBox]) rather than a fabricated
 * number. That is the only "loading" signal UI layers get for fiat values; there are no
 * bootstrap or fallback rates elsewhere in the codebase.
 *
 * [approximate] prepends "≈ " — used for derived values (fees, totals, USD equivalents of a
 * crypto amount) to signal to the reader that the number is indicative, not a contract price.
 */
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
