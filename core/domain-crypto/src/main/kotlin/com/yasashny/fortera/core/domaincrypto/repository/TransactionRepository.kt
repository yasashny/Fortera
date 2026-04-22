package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(
        token: TokenDefinition,
        ethAddress: String,
        btcAddress: String,
        limit: Int = 10,
    ): Result<List<Transaction>>
}
