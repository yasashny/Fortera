package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.TokenBalance

data class BalanceResult(
    val balances: List<TokenBalance>,
    val failedNetworks: Set<String>,
)

/**
 * Source of truth for wallet balances. Combines on-chain queries with price data, handles
 * partial failures, and manages its own on-disk cache — callers never touch the cache
 * directly, they ask for balances and receive them.
 */
interface BalanceRepository {
    /** Fetch balances, caching them on success. [policy] controls cache vs remote behavior. */
    suspend fun getTokenBalances(
        walletId: String,
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
        policy: FetchPolicy = FetchPolicy.CacheFirst,
    ): Result<BalanceResult>

    /** Returns previously cached balances without touching the network, or null if cache is empty. */
    suspend fun getCachedBalances(
        walletId: String,
        enabledTokenIds: Set<String>,
    ): List<TokenBalance>?

    /** Wipe every cached balance across all wallets. Typically called on environment switch. */
    suspend fun clearAllCaches()
}
