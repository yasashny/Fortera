package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.datacrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.datacrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.model.Transaction
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepository

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
        val contract = token.contractAddress
        when {
            token.network == BlockchainNetwork.BITCOIN ->
                blockstreamDataSource.getTransactions(btcAddress, limit)

            contract != null ->
                infuraDataSource.getErc20Transactions(
                    address = ethAddress,
                    contractAddress = contract,
                    symbol = token.symbol,
                    decimals = token.decimals,
                    limit = limit,
                )

            else -> infuraDataSource.getEthTransactions(ethAddress, limit)
        }
    }
}
