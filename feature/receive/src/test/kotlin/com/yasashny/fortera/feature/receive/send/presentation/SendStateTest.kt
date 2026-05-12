package com.yasashny.fortera.feature.receive.send.presentation

import com.yasashny.fortera.core.ui.text.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SendStateTest {

    @Test
    fun `normalizeDecimal replaces comma with dot`() {
        assertEquals("1.5", "1,5".normalizeDecimal())
    }

    @Test
    fun `normalizeDecimal keeps existing dot`() {
        assertEquals("2.75", "2.75".normalizeDecimal())
    }

    @Test
    fun `isAmountValid returns false when amount is blank`() {
        val state = SendState(amount = "")

        assertFalse(state.isAmountValid)
    }

    @Test
    fun `isAmountValid returns false for zero amount`() {
        val state = SendState(amount = "0")

        assertFalse(state.isAmountValid)
    }

    @Test
    fun `isAmountValid returns true for positive decimal`() {
        val state = SendState(amount = "0.001")

        assertTrue(state.isAmountValid)
    }

    @Test
    fun `isAmountValid normalizes comma to dot`() {
        val state = SendState(amount = "0,5")

        assertTrue(state.isAmountValid)
    }

    @Test
    fun `canContinue requires non-blank address`() {
        val state = SendState(amount = "1", address = "  ")

        assertFalse(state.canContinue)
    }

    @Test
    fun `canContinue is false when funds are insufficient`() {
        val state = SendState(
            amount = "1",
            address = "0xabc",
            insufficientFunds = true,
        )

        assertFalse(state.canContinue)
    }

    @Test
    fun `canContinue is false when amount or address has an error`() {
        val withAmountError = SendState(
            amount = "1",
            address = "0xabc",
            amountError = UiText.of("err"),
        )
        val withAddressError = SendState(
            amount = "1",
            address = "0xabc",
            addressError = UiText.of("err"),
        )

        assertFalse(withAmountError.canContinue)
        assertFalse(withAddressError.canContinue)
    }

    @Test
    fun `canContinue is true when everything is valid`() {
        val state = SendState(
            balance = BigDecimal("10"),
            amount = "1.5",
            address = "0xabc",
            insufficientFunds = false,
            amountError = null,
            addressError = null,
        )

        assertTrue(state.canContinue)
    }
}
