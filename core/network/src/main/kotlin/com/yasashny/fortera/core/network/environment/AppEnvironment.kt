package com.yasashny.fortera.core.network.environment

enum class AppEnvironment(
    val displayName: String,
    val ethChainId: Long,
    val ethHost: String,
    val ethBlockscoutBase: String,
    val btcEsploraBase: String,
    val isTestnet: Boolean,
) {
    MAINNET(
        displayName = "Mainnet",
        ethChainId = 1L,
        ethHost = "mainnet.infura.io",
        ethBlockscoutBase = "https://eth.blockscout.com",
        btcEsploraBase = "https://blockstream.info/api",
        isTestnet = false,
    ),
    TESTNET(
        displayName = "Testnet",
        ethChainId = 11155111L,
        ethHost = "sepolia.infura.io",
        ethBlockscoutBase = "https://eth-sepolia.blockscout.com",
        btcEsploraBase = "https://blockstream.info/testnet/api",
        isTestnet = true,
    ),
}
