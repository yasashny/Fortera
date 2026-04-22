package com.yasashny.fortera.core.walletbalances

/** ETH + BTC addresses derived from a wallet's seed phrase. */
data class WalletAddresses(
    val eth: String,
    val btc: String,
)
