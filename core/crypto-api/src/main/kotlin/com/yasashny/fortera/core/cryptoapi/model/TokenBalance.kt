package com.yasashny.fortera.core.cryptoapi.model

import java.math.BigDecimal

data class TokenBalance(
    val token: TokenDefinition,
    val balance: BigDecimal,
    val priceUsd: Double,
    val changePercent24h: Double,
) {
    val balanceUsd: Double get() = balance.toDouble() * priceUsd
}
