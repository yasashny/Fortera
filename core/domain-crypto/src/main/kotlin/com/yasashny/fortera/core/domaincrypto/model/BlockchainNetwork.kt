package com.yasashny.fortera.core.domaincrypto.model

enum class BlockchainNetwork(
    val displayName: String,
    val nativeSymbol: String,
) {
    BITCOIN(displayName = "Bitcoin", nativeSymbol = "BTC"),
    ETHEREUM(displayName = "Ethereum", nativeSymbol = "ETH"),
}
