package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.db.TokenDao
import com.yasashny.fortera.core.domaincrypto.db.WalletTokenEntity
import com.yasashny.fortera.core.domaincrypto.db.toEntity
import com.yasashny.fortera.core.domaincrypto.db.toTokenDefinition
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TokenRepository {
    suspend fun getAllTokens(): List<TokenDefinition>
    suspend fun getTokenById(id: String): TokenDefinition?
    suspend fun getEnabledTokenIds(walletId: String): Set<String>
    fun observeEnabledTokenIds(walletId: String): Flow<Set<String>>
    suspend fun setTokenEnabled(walletId: String, tokenId: String, enabled: Boolean)
}

internal class TokenRepositoryImpl(
    private val tokenDao: TokenDao,
) : TokenRepository {

    private suspend fun ensureSeeded() {
        if (tokenDao.getTokenCount() == 0) {
            tokenDao.insertTokens(TokenCatalog.tokens.map { it.toEntity() })
        }
    }

    private suspend fun ensureWalletSeeded(walletId: String) {
        if (tokenDao.countWalletTokens(walletId) == 0) {
            ensureSeeded()
            val defaults = tokenDao.getDefaultTokenIds()
            tokenDao.insertWalletTokens(defaults.map { WalletTokenEntity(walletId, it) })
        }
    }

    override suspend fun getAllTokens(): List<TokenDefinition> {
        ensureSeeded()
        return tokenDao.getAllTokens().map { it.toTokenDefinition() }
    }

    override suspend fun getTokenById(id: String): TokenDefinition? {
        ensureSeeded()
        return tokenDao.getTokenById(id)?.toTokenDefinition()
    }

    override suspend fun getEnabledTokenIds(walletId: String): Set<String> {
        ensureWalletSeeded(walletId)
        return tokenDao.getEnabledTokenIds(walletId).toSet()
    }

    override fun observeEnabledTokenIds(walletId: String): Flow<Set<String>> {
        return tokenDao.observeEnabledTokenIds(walletId).map { it.toSet() }
    }

    override suspend fun setTokenEnabled(walletId: String, tokenId: String, enabled: Boolean) {
        if (enabled) {
            tokenDao.insertWalletTokens(listOf(WalletTokenEntity(walletId, tokenId)))
        } else {
            tokenDao.deleteWalletToken(walletId, tokenId)
        }
    }
}
