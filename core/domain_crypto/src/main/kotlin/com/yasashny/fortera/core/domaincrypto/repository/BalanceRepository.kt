package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal

private val ERC20_DECIMALS = mapOf(
    "USDT" to 6,
    "USDC" to 6,
)

interface BalanceRepository {
    suspend fun getTokenBalances(
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
        forceRemote: Boolean = false,
    ): Result<List<TokenBalance>>
}

internal class BalanceRepositoryImpl(
    private val infuraDataSource: InfuraDataSource,
    private val blockstreamDataSource: BlockstreamDataSource,
    private val priceRepository: PriceRepository,
) : BalanceRepository {

    override suspend fun getTokenBalances(
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
        forceRemote: Boolean,
    ): Result<List<TokenBalance>> = runCatching {
        val tokens = TokenCatalog.tokens.filter { it.id in enabledTokenIds }

        coroutineScope {
            val balanceDeferred = async { fetchBalances(tokens, ethAddress, btcAddress) }
            val coinIds = tokens.map { it.id }
            val pricesDeferred = async {
                if (forceRemote) priceRepository.getPricesRemote(coinIds)
                else priceRepository.getPricesLocalOrRemote(coinIds)
            }

            val balances = balanceDeferred.await()
            val prices = pricesDeferred.await()

            tokens.map { token ->
                val balance = balances[token.id] ?: BigDecimal.ZERO
                val priceInfo = prices[token.id]
                TokenBalance(
                    token = token,
                    balance = balance,
                    priceUsd = priceInfo?.priceUsd ?: 0.0,
                    changePercent24h = priceInfo?.changePercent24h ?: 0.0,
                )
            }
        }
    }

    private suspend fun fetchBalances(
        tokens: List<TokenDefinition>,
        ethAddress: String,
        btcAddress: String,
    ): Map<String, BigDecimal> = coroutineScope {
        tokens.map { token ->
            async {
                val balance = runCatching {
                    when {
                        token.network == BlockchainNetwork.BITCOIN ->
                            blockstreamDataSource.getBtcBalance(btcAddress)
                        token.contractAddress == null ->
                            infuraDataSource.getEthBalance(ethAddress)
                        else -> {
                            val decimals = ERC20_DECIMALS[token.symbol] ?: 18
                            infuraDataSource.getErc20Balance(ethAddress, token.contractAddress, decimals)
                        }
                    }
                }.getOrElse { BigDecimal.ZERO }
                token.id to balance
            }
        }.associate { it.await() }
    }
}
