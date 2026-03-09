package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.model.Transaction

private val ERC20_DECIMALS = mapOf(
    "USDT" to 6,
    "USDC" to 6,
)

interface TransactionRepository {
    suspend fun getTransactions(
        token: TokenDefinition,
        ethAddress: String,
        btcAddress: String,
        limit: Int = 10,
    ): Result<List<Transaction>>
}

internal class TransactionRepositoryImpl(
    private val infuraDataSource: InfuraDataSource,
    private val blockstreamDataSource: BlockstreamDataSource,
) : TransactionRepository {

    override suspend fun getTransactions(
        token: TokenDefinition,
        ethAddress: String,
        btcAddress: String,
        limit: Int,
    ): Result<List<Transaction>> = runCatching {
        when {
            token.network == BlockchainNetwork.BITCOIN ->
                blockstreamDataSource.getTransactions(btcAddress, limit)

            token.contractAddress != null -> {
                val decimals = ERC20_DECIMALS[token.symbol] ?: 18
                infuraDataSource.getErc20Transactions(
                    address = ethAddress,
                    contractAddress = token.contractAddress,
                    symbol = token.symbol,
                    decimals = decimals,
                    limit = limit,
                )
            }

            else -> infuraDataSource.getEthTransactions(ethAddress, limit)
        }
    }
}
