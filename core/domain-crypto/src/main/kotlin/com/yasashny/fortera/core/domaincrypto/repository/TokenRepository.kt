package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    suspend fun getAllTokens(): List<TokenDefinition>
    fun observeAllTokens(): Flow<List<TokenDefinition>>
    suspend fun getTokenById(id: String): TokenDefinition?
    suspend fun findTokenByContract(contractAddress: String): TokenDefinition?
    suspend fun addCustomToken(token: TokenDefinition)
    suspend fun getEnabledTokenIds(walletId: String): Set<String>
    fun observeEnabledTokenIds(walletId: String): Flow<Set<String>>
    suspend fun setTokenEnabled(walletId: String, tokenId: String, enabled: Boolean)
}
