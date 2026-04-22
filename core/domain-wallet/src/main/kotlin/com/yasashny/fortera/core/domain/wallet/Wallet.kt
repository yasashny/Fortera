package com.yasashny.fortera.core.domain.wallet

data class Wallet(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@JvmInline
value class SeedPhrase(val words: List<String>) {

    val wordCount: Int get() = words.size

    fun toDisplayString(): String = words.joinToString(" ")

    companion object {
        const val STANDARD_WORD_COUNT = 12
        const val EXTENDED_WORD_COUNT = 24
    }
}
