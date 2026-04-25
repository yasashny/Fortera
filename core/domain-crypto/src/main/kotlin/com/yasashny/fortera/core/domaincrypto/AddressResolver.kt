package com.yasashny.fortera.core.domaincrypto

interface AddressResolver {
    fun ethAddress(mnemonic: String): String

    suspend fun btcAddress(mnemonic: String): String
}
