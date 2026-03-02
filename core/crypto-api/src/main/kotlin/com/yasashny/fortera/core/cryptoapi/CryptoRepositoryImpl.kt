package com.yasashny.fortera.core.cryptoapi

import com.yasashny.fortera.core.cryptoapi.client.BlockstreamClient
import com.yasashny.fortera.core.cryptoapi.client.CoinGeckoClient
import com.yasashny.fortera.core.cryptoapi.client.InfuraClient
import com.yasashny.fortera.core.cryptoapi.model.BlockchainNetwork
import com.yasashny.fortera.core.cryptoapi.model.TokenBalance
import com.yasashny.fortera.core.cryptoapi.model.TokenDefinition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.math.BigDecimal

// ERC-20 decimal places by token symbol
private val ERC20_DECIMALS = mapOf(
    "USDT" to 6,
    "USDC" to 6,
)

internal class CryptoRepositoryImpl(
    private val infuraClient: InfuraClient,
    private val blockstreamClient: BlockstreamClient,
    private val coinGeckoClient: CoinGeckoClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CryptoRepository {

    override suspend fun getTokenBalances(
        ethAddress: String,
        btcAddress: String,
        enabledTokenIds: Set<String>,
    ): Result<List<TokenBalance>> = runCatching {
        val tokens = TokenCatalog.tokens.filter { it.id in enabledTokenIds }

        withContext(ioDispatcher) {
            coroutineScope {
                val balanceDeferred = async { fetchBalances(tokens, ethAddress, btcAddress) }
                val pricesDeferred = async {
                    coinGeckoClient.getPrices(tokens.map { it.coingeckoId })
                }

                val balances = balanceDeferred.await()
                val prices = pricesDeferred.await()

                tokens.map { token ->
                    val balance = balances[token.id] ?: BigDecimal.ZERO
                    val (price, change) = prices[token.coingeckoId] ?: (0.0 to 0.0)
                    TokenBalance(
                        token = token,
                        balance = balance,
                        priceUsd = price,
                        changePercent24h = change,
                    )
                }
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
                            blockstreamClient.getBtcBalance(btcAddress)
                        token.contractAddress == null ->
                            infuraClient.getEthBalance(ethAddress)
                        else -> {
                            val decimals = ERC20_DECIMALS[token.symbol] ?: 18
                            infuraClient.getErc20Balance(ethAddress, token.contractAddress, decimals)
                        }
                    }
                }.getOrElse { BigDecimal.ZERO }
                token.id to balance
            }
        }.associate { it.await() }
    }
}
