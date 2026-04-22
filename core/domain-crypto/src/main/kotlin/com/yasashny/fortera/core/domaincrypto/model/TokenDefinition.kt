package com.yasashny.fortera.core.domaincrypto.model

/**
 * Definition of a token tracked by the app.
 *
 * [decimals] is the on-chain unit scale:
 * - Native ETH / ETH-compatible: 18
 * - Most ERC-20: 18
 * - USDT / USDC (ERC-20): 6
 * - Native BTC: 8 (satoshis)
 *
 * Always prefer this field over hard-coded maps by symbol.
 */
data class TokenDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val network: BlockchainNetwork,
    val decimals: Int,
    val contractAddress: String? = null,
    val coingeckoId: String,
    val isDefault: Boolean = false,
)
