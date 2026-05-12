package com.yasashny.fortera.core.ui.text

import org.junit.Assert.assertEquals
import org.junit.Test

class UiTextTest {

    @Test
    fun `of literal returns Literal variant`() {
        val text = UiText.of("hello")

        assertEquals(UiText.Literal("hello"), text)
    }

    @Test
    fun `of resource without args returns Resource with empty args`() {
        val text = UiText.of(42)

        assertEquals(UiText.Resource(42, emptyList()), text)
    }

    @Test
    fun `of resource preserves vararg args in order`() {
        val text = UiText.of(7, "alpha", 99)

        assertEquals(UiText.Resource(7, listOf<Any>("alpha", 99)), text)
    }

    @Test
    fun `two Literals with the same text are equal`() {
        assertEquals(UiText.of("x"), UiText.of("x"))
    }
}
