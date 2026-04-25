package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.datacrypto.repository.send.BitcoinSender
import com.yasashny.fortera.core.datacrypto.repository.send.EthereumSender
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepository
import java.math.BigDecimal

internal class SendTransactionRepositoryImpl(
    private val ethereumSender: EthereumSender,
    private val bitcoinSender: BitcoinSender,
) : SendTransactionRepository {

    override suspend fun estimateFees(
        token: TokenDefinition,
        fromAddress: String?,
        amount: BigDecimal,
    ): Result<FeeEstimates> = runCatching {
        when (token.network) {
            BlockchainNetwork.ETHEREUM -> ethereumSender.estimateFees(token, fromAddress, amount)
            BlockchainNetwork.BITCOIN -> bitcoinSender.estimateFees(fromAddress, amount)
        }
    }

    override suspend fun send(
        mnemonic: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): Result<String> = runCatching {
        when (token.network) {
            BlockchainNetwork.ETHEREUM -> ethereumSender.send(mnemonic, token, toAddress, amount, speed)
            BlockchainNetwork.BITCOIN -> bitcoinSender.send(mnemonic, toAddress, amount, speed)
        }
    }
}
