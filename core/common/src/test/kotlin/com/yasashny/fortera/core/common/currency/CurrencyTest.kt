package com.yasashny.fortera.core.common.currency

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyTest {

    @Test
    fun `Default is USD`() {
        assertEquals(Currency.USD, Currency.Default)
    }

    @Test
    fun `fromCodeOrDefault matches a known code`() {
        assertEquals(Currency.RUB, Currency.fromCodeOrDefault("RUB"))
        assertEquals(Currency.EUR, Currency.fromCodeOrDefault("EUR"))
        assertEquals(Currency.CNY, Currency.fromCodeOrDefault("CNY"))
    }

    @Test
    fun `fromCodeOrDefault returns Default for unknown code`() {
        assertEquals(Currency.Default, Currency.fromCodeOrDefault("XYZ"))
    }

    @Test
    fun `fromCodeOrDefault returns Default for null`() {
        assertEquals(Currency.Default, Currency.fromCodeOrDefault(null))
    }

    @Test
    fun `fromCodeOrDefault is case-sensitive`() {
        assertEquals(Currency.Default, Currency.fromCodeOrDefault("rub"))
    }

    @Test
    fun `each currency exposes the expected symbol position`() {
        assertEquals(CurrencySymbolPosition.PREFIX, Currency.USD.symbolPosition)
        assertEquals(CurrencySymbolPosition.PREFIX, Currency.EUR.symbolPosition)
        assertEquals(CurrencySymbolPosition.PREFIX, Currency.CNY.symbolPosition)
        assertEquals(CurrencySymbolPosition.SUFFIX, Currency.RUB.symbolPosition)
    }
}
