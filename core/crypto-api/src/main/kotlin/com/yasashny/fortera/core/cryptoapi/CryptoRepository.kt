package com.yasashny.fortera.core.cryptoapi

import com.yasashny.fortera.core.cryptoapi.model.TokenBalance

interface CryptoRepository {
    suspend fun getTokenBalances(
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
    ): Result<List<TokenBalance>>
}
