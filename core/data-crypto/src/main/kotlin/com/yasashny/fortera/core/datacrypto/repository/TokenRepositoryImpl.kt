package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.datacrypto.db.TokenDao
import com.yasashny.fortera.core.datacrypto.db.WalletTokenEntity
import com.yasashny.fortera.core.datacrypto.db.toEntity
import com.yasashny.fortera.core.datacrypto.db.toTokenDefinition
import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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

    override fun observeAllTokens(): Flow<List<TokenDefinition>> = flow {
        ensureSeeded()
        emitAll(tokenDao.observeAllTokens().map { entities ->
            entities.map { it.toTokenDefinition() }
        })
    }

    override suspend fun getTokenById(id: String): TokenDefinition? {
        ensureSeeded()
        return tokenDao.getTokenById(id)?.toTokenDefinition()
    }

    override suspend fun findTokenByContract(contractAddress: String): TokenDefinition? {
        ensureSeeded()
        return tokenDao.getTokenByContract(contractAddress)?.toTokenDefinition()
    }

    override suspend fun addCustomToken(token: TokenDefinition) {
        ensureSeeded()
        tokenDao.insertTokens(listOf(token.toEntity()))
    }

    override suspend fun getEnabledTokenIds(walletId: String): Set<String> {
        ensureWalletSeeded(walletId)
        return tokenDao.getEnabledTokenIds(walletId).toSet()
    }

    override fun observeEnabledTokenIds(walletId: String): Flow<Set<String>> = flow {
        ensureWalletSeeded(walletId)
        emitAll(tokenDao.observeEnabledTokenIds(walletId).map { it.toSet() })
    }

    override suspend fun setTokenEnabled(walletId: String, tokenId: String, enabled: Boolean) {
        if (enabled) {
            tokenDao.insertWalletTokens(listOf(WalletTokenEntity(walletId, tokenId)))
        } else {
            tokenDao.deleteWalletToken(walletId, tokenId)
        }
    }
}
