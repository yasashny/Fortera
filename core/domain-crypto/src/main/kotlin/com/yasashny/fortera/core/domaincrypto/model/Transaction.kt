package com.yasashny.fortera.core.domaincrypto.model

import java.math.BigDecimal

data class Transaction(
    val hash: String,
    val timestampSeconds: Long,
    val from: String,
    val to: String,
    val amount: BigDecimal,
    val symbol: String,
    val isIncoming: Boolean,
    val confirmed: Boolean,
)
