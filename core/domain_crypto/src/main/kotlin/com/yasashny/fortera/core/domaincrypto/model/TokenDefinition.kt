package com.yasashny.fortera.core.domaincrypto.model

data class TokenDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val network: BlockchainNetwork,
    val contractAddress: String? = null,
    val coingeckoId: String,
    val isDefault: Boolean = false,
)
