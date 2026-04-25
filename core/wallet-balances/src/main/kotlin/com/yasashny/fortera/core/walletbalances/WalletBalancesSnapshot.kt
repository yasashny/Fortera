package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domaincrypto.model.TokenBalance

data class WalletBalancesSnapshot(
    val walletId: String,
    val balances: List<TokenBalance>,
    val totalUsd: Double,
    val unreachableNetworks: Set<String>,
    val isFromCache: Boolean,
) {
    val isFullyReachable: Boolean get() = unreachableNetworks.isEmpty()
}
