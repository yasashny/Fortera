package com.yasashny.fortera.core.domaincrypto.model

/**
 * Supported L1 networks.
 *
 * [displayName] is used for user-facing labels (preview, selectors, banners).
 * [nativeSymbol] is the ticker of the network's base currency — used to tag fee estimates
 * and native-asset transactions so wallet logic can compare symbols without hard-coding.
 */
enum class BlockchainNetwork(
    val displayName: String,
    val nativeSymbol: String,
) {
    BITCOIN(displayName = "Bitcoin", nativeSymbol = "BTC"),
    ETHEREUM(displayName = "Ethereum", nativeSymbol = "ETH"),
}
