package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.TokenBalance

data class BalanceResult(
    val balances: List<TokenBalance>,
    val failedNetworks: Set<String>,
)

interface BalanceRepository {
    suspend fun getTokenBalances(
        walletId: String,
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
        policy: FetchPolicy = FetchPolicy.CacheFirst,
    ): Result<BalanceResult>

    suspend fun getCachedBalances(
        walletId: String,
        enabledTokenIds: Set<String>,
    ): List<TokenBalance>?

    suspend fun clearAllCaches()
}
