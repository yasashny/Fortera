package com.yasashny.fortera.core.domaincrypto.model

data class CustomTokenMetadata(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val contractAddress: String,
    val network: BlockchainNetwork,
    val coingeckoId: String?,
)
