package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.datacrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.datacrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.datacrypto.db.CachedBalanceEntity
import com.yasashny.fortera.core.datacrypto.db.TokenDao
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.BalanceResult
import com.yasashny.fortera.core.domaincrypto.repository.FetchPolicy
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.math.BigDecimal

private const val MAX_RETRIES = 3
private const val RETRY_DELAY_MS = 1000L

internal class BalanceRepositoryImpl(
    private val infuraDataSource: InfuraDataSource,
    private val blockstreamDataSource: BlockstreamDataSource,
    private val priceRepository: PriceRepository,
    private val tokenRepository: TokenRepository,
    private val tokenDao: TokenDao,
) : BalanceRepository {

    override suspend fun getTokenBalances(
        walletId: String,
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
        policy: FetchPolicy,
    ): Result<BalanceResult> = runCatching {
        val tokens = tokenRepository.getAllTokens().filter { it.id in enabledTokenIds }

        coroutineScope {
            val balanceDeferred = async { fetchBalances(tokens, ethAddress, btcAddress) }
            val coinIds = tokens.map { it.id }
            val pricesDeferred = async { priceRepository.getPrices(coinIds, policy) }

            val balances = balanceDeferred.await()
            val prices = pricesDeferred.await()

            val failedTokenIds = balances.filter { it.value == null }.keys
            val failedNetworks = tokens
                .filter { it.id in failedTokenIds }
                .map { it.network.displayName }
                .toSet()

            val cachedMap = if (failedTokenIds.isNotEmpty()) {
                tokenDao.getCachedBalances(walletId, failedTokenIds.toList())
                    .associate { it.tokenId to it }
            } else {
                emptyMap()
            }

            val tokenBalances = tokens.map { token ->
                val priceInfo = prices[token.id]
                val fetched = balances[token.id]
                if (fetched != null) {
                    TokenBalance(
                        token = token,
                        balance = fetched,
                        priceUsd = priceInfo?.priceUsd ?: 0.0,
                        changePercent24h = priceInfo?.changePercent24h ?: 0.0,
                    )
                } else {
                    val cached = cachedMap[token.id]
                    TokenBalance(
                        token = token,
                        balance = cached?.balance?.toBigDecimal() ?: BigDecimal.ZERO,
                        priceUsd = priceInfo?.priceUsd ?: cached?.priceUsd ?: 0.0,
                        changePercent24h = priceInfo?.changePercent24h
                            ?: cached?.changePercent24h ?: 0.0,
                    )
                }
            }

            persistCache(walletId, tokenBalances)

            BalanceResult(balances = tokenBalances, failedNetworks = failedNetworks)
        }
    }

    override suspend fun getCachedBalances(
        walletId: String,
        enabledTokenIds: Set<String>,
    ): List<TokenBalance>? {
        val cached = tokenDao.getCachedBalances(walletId, enabledTokenIds.toList())
        if (cached.isEmpty()) return null

        return cached.mapNotNull { entity ->
            val token = tokenRepository.getTokenById(entity.tokenId) ?: return@mapNotNull null
            TokenBalance(
                token = token,
                balance = entity.balance.toBigDecimal(),
                priceUsd = entity.priceUsd,
                changePercent24h = entity.changePercent24h,
            )
        }.ifEmpty { null }
    }

    override suspend fun clearAllCaches() {
        tokenDao.clearAllCachedBalances()
    }

    private suspend fun persistCache(walletId: String, balances: List<TokenBalance>) {
        val entities = balances.map { tb ->
            CachedBalanceEntity(
                walletId = walletId,
                tokenId = tb.token.id,
                balance = tb.balance.toPlainString(),
                priceUsd = tb.priceUsd,
                changePercent24h = tb.changePercent24h,
            )
        }
        tokenDao.insertCachedBalances(entities)
    }

    private suspend fun fetchBalances(
        tokens: List<TokenDefinition>,
        ethAddress: String,
        btcAddress: String,
    ): Map<String, BigDecimal?> = coroutineScope {
        tokens.map { token ->
            async {
                val contract = token.contractAddress
                val balance = fetchWithRetry {
                    when {
                        token.network == BlockchainNetwork.BITCOIN ->
                            blockstreamDataSource.getBtcBalance(btcAddress)
                        contract == null ->
                            infuraDataSource.getEthBalance(ethAddress)
                        else ->
                            infuraDataSource.getErc20Balance(ethAddress, contract, token.decimals)
                    }
                }
                token.id to balance
            }
        }.associate { it.await() }
    }

    private suspend fun fetchWithRetry(block: suspend () -> BigDecimal): BigDecimal? {
        repeat(MAX_RETRIES) { attempt ->
            try {
                return block()
            } catch (_: Exception) {
                if (attempt < MAX_RETRIES - 1) delay(RETRY_DELAY_MS)
            }
        }
        return null
    }
}
