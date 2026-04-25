package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import java.math.BigDecimal

interface SendTransactionRepository {
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
