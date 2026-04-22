package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepository
import java.math.BigDecimal

/**
 * Send-transaction operations scoped to a wallet — the caller identifies the wallet by id,
 * seed access stays inside the bridge module.
 *
 * Without this, every "send" feature has to pull the seed phrase and pass it to
 * [SendTransactionRepository.send] directly, which means the raw mnemonic travels through
 * presentation code.
 */
interface WalletTransactionSender {
    suspend fun estimateFees(token: TokenDefinition): Result<FeeEstimates>

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
    private val sendTransactionRepository: SendTransactionRepository,
) : WalletTransactionSender {

    override suspend fun estimateFees(token: TokenDefinition): Result<FeeEstimates> =
        sendTransactionRepository.estimateFees(token)

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
