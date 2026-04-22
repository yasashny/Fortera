package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domaincrypto.model.TokenBalance

/**
 * Point-in-time view of a wallet's balances.
 *
 * [isFromCache] — balances were served from disk without touching the network.
 * [unreachableNetworks] — networks whose upstream API failed; listed entries have fallback values
 *                         (cached or zero) rather than fresh on-chain data.
 */
data class WalletBalancesSnapshot(
    val walletId: String,
    val balances: List<TokenBalance>,
    val totalUsd: Double,
    val unreachableNetworks: Set<String>,
    val isFromCache: Boolean,
) {
    val isFullyReachable: Boolean get() = unreachableNetworks.isEmpty()
}
