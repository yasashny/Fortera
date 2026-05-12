package com.yasashny.fortera.core.domaincrypto.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class TokenBalanceTest {

    private val btc = TokenDefinition(
        id = "bitcoin",
        name = "Bitcoin",
        symbol = "BTC",
        network = BlockchainNetwork.BITCOIN,
        decimals = 8,
        contractAddress = null,
        coingeckoId = "bitcoin",
        isDefault = true,
    )

    @Test
    fun `balanceUsd multiplies balance by priceUsd`() {
        val balance = TokenBalance(
            token = btc,
            balance = BigDecimal("0.5"),
            priceUsd = 100.0,
            changePercent24h = 0.0,
        )

        assertEquals(50.0, balance.balanceUsd, 0.0001)
    }

    @Test
    fun `balanceUsd is zero when price is zero`() {
        val balance = TokenBalance(
            token = btc,
            balance = BigDecimal("10"),
            priceUsd = 0.0,
            changePercent24h = 0.0,
        )

        assertEquals(0.0, balance.balanceUsd, 0.0)
    }

    @Test
    fun `balanceUsd is zero when balance is zero`() {
        val balance = TokenBalance(
            token = btc,
            balance = BigDecimal.ZERO,
            priceUsd = 50_000.0,
            changePercent24h = 0.0,
        )

        assertEquals(0.0, balance.balanceUsd, 0.0)
    }
}
