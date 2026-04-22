package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import java.math.BigDecimal

interface SendTransactionRepository {
    /**
     * Estimate fees for sending [amount] of [token] from [fromAddress] (nullable — when null
     * the implementation uses conservative templates).
     *
     * [amount] lets BTC pre-select UTXOs for realistic vsize and ETH call `eth_estimateGas`
     * for per-token ERC-20 gas, producing much more accurate fees than a fixed template.
     */
    suspend fun estimateFees(
        token: TokenDefinition,
        fromAddress: String?,
        amount: BigDecimal,
    ): Result<FeeEstimates>

    suspend fun send(
        mnemonic: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): Result<String>
}
