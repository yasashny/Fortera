package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepository
import java.math.BigDecimal

interface WalletTransactionSender {
    suspend fun estimateFees(
        walletId: String,
        token: TokenDefinition,
        amount: BigDecimal,
    ): Result<FeeEstimates>

    suspend fun send(
        walletId: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): Result<String>
}

internal class WalletTransactionSenderImpl(
    private val walletInteractor: WalletInteractor,
    private val addresses: WalletAddressesService,
    private val sendTransactionRepository: SendTransactionRepository,
) : WalletTransactionSender {

    override suspend fun estimateFees(
        walletId: String,
        token: TokenDefinition,
        amount: BigDecimal,
    ): Result<FeeEstimates> {
        val walletAddresses = runCatching { addresses.forWallet(walletId) }.getOrNull()
        val fromAddress = when (token.network) {
            BlockchainNetwork.BITCOIN -> walletAddresses?.btc
            BlockchainNetwork.ETHEREUM -> walletAddresses?.eth
        }
        return sendTransactionRepository.estimateFees(token, fromAddress, amount)
    }

    override suspend fun send(
        walletId: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): Result<String> {
        val seed = walletInteractor.getSeedPhrase(walletId).getOrNull()?.toDisplayString()
            ?: return Result.failure(IllegalStateException("Seed phrase unavailable for wallet $walletId"))
        return sendTransactionRepository.send(seed, token, toAddress, amount, speed)
    }
}
