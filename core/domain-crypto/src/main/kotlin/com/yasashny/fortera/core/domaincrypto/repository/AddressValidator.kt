package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork

/**
 * Validates on-chain addresses per [BlockchainNetwork] semantics.
 *
 * - Ethereum: `0x` + 40 hex chars (no checksum required).
 * - Bitcoin: bech32 SegWit (`bc1…` on mainnet, `tb1…` on testnet) — network-aware.
 *
 * The function is `suspend` because BTC validation needs the current environment
 * (mainnet vs testnet) which is read from a suspending store.
 */
interface AddressValidator {
    suspend fun isValid(address: String, network: BlockchainNetwork): Boolean
}
