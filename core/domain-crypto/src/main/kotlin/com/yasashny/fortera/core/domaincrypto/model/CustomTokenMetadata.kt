package com.yasashny.fortera.core.domaincrypto.model

/**
 * Metadata used to populate a [TokenDefinition] when the user adds a token manually —
 * either by ERC-20 contract address (fetched on-chain) or by coin-listing ID (fetched
 * from a public coin index such as CoinGecko).
 *
 * [coingeckoId] may be null when the on-chain lookup succeeds but the contract isn't
 * indexed by any price service — the token can still be tracked for balance, it just
 * won't show a USD price.
 */
data class CustomTokenMetadata(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val contractAddress: String,
    val network: BlockchainNetwork,
    val coingeckoId: String?,
)
