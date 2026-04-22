package com.yasashny.fortera.core.domaincrypto

/**
 * Derives on-chain addresses from a BIP-39 mnemonic.
 *
 * Domain contract; implementation lives in the data layer (backed by bitcoinj / web3j).
 * Callers that only need addresses should not touch [AddressResolver] directly — the
 * `core:wallet_balances` module wraps this behind a wallet-centric API that never exposes
 * the raw mnemonic to presentation code.
 */
interface AddressResolver {
    /** Pure BIP-32 derivation. */
    fun ethAddress(mnemonic: String): String

    /** Suspends because BTC params depend on the current environment. */
    suspend fun btcAddress(mnemonic: String): String
}
