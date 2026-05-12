package com.yasashny.fortera.core.domain.wallet

import org.junit.Assert.assertEquals
import org.junit.Test

class SeedPhraseTest {

    @Test
    fun `wordCount returns number of words`() {
        val phrase = SeedPhrase(listOf("a", "b", "c"))
        assertEquals(3, phrase.wordCount)
    }

    @Test
    fun `toDisplayString joins words with single space`() {
        val phrase = SeedPhrase(listOf("alpha", "beta", "gamma"))
        assertEquals("alpha beta gamma", phrase.toDisplayString())
    }

    @Test
    fun `empty SeedPhrase has zero words`() {
        val phrase = SeedPhrase(emptyList())
        assertEquals(0, phrase.wordCount)
        assertEquals("", phrase.toDisplayString())
    }

    @Test
    fun `constants expose the supported word counts`() {
        assertEquals(12, SeedPhrase.STANDARD_WORD_COUNT)
        assertEquals(24, SeedPhrase.EXTENDED_WORD_COUNT)
    }
}
