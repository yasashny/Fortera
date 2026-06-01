package com.yasashny.fortera.core.ui.format

import com.yasashny.fortera.core.common.currency.Currency
import com.yasashny.fortera.core.ui.currency.FiatDisplay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class MoneyFormatTest {

    @Test
    fun `formatFiat returns null when no exchange rate is known`() {
        val display = FiatDisplay(Currency.USD, rateFromUsd = null)

        assertNull(formatFiat(amountUsd = 100.0, fiat = display))
    }

    @Test
    fun `formatFiat prefixes USD with dollar sign`() {
        val display = FiatDisplay(Currency.USD, rateFromUsd = 1.0)

        assertEquals("$1 234,56", formatFiat(amountUsd = 1_234.56, fiat = display))
    }

    @Test
    fun `formatFiat applies conversion rate when not USD`() {
        val display = FiatDisplay(Currency.RUB, rateFromUsd = 90.0)

        assertEquals("9 000,00 ₽", formatFiat(amountUsd = 100.0, fiat = display))
    }

    @Test
    fun `formatFiat uses EUR prefix`() {
        val display = FiatDisplay(Currency.EUR, rateFromUsd = 0.5)

        assertEquals("€50,00", formatFiat(amountUsd = 100.0, fiat = display))
    }

    @Test
    fun `formatFiat with approximate prepends the ≈ marker`() {
        val display = FiatDisplay(Currency.USD, rateFromUsd = 1.0)

        assertEquals("≈ $10,00", formatFiat(amountUsd = 10.0, fiat = display, approximate = true))
    }

    @Test
    fun `formatFiat keeps significant digits for sub-cent prices`() {
        val display = FiatDisplay(Currency.USD, rateFromUsd = 1.0)

        assertEquals("$0,00001226", formatFiat(amountUsd = 0.00001226, fiat = display))
        assertEquals("$0,005", formatFiat(amountUsd = 0.005, fiat = display))
    }

    @Test
    fun `formatFiat still uses two decimals at or above one cent`() {
        val display = FiatDisplay(Currency.USD, rateFromUsd = 1.0)

        assertEquals("$0,01", formatFiat(amountUsd = 0.01, fiat = display))
        assertEquals("$0,50", formatFiat(amountUsd = 0.5, fiat = display))
        assertEquals("$0,00", formatFiat(amountUsd = 0.0, fiat = display))
    }

    @Test
    fun `formatCrypto preserves values shorter than 7 decimals`() {
        assertEquals("0.5", formatCrypto(BigDecimal("0.5")))
        assertEquals("12.345", formatCrypto(BigDecimal("12.345")))
        assertEquals("123", formatCrypto(BigDecimal("123")))
    }

    @Test
    fun `formatCrypto truncates very long fractional parts to 6 decimals`() {
        // The formatter cuts at dot + 7 chars, which keeps 6 fractional digits.
        assertEquals("0.123456", formatCrypto(BigDecimal("0.123456789")))
    }

    @Test
    fun `formatCrypto Double overload routes through BigDecimal`() {
        assertEquals("0.25", formatCrypto(0.25))
    }
}
