package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.feature.main.domain.MainOverview
import com.yasashny.fortera.feature.main.domain.MainOverviewEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MainReducerTest {

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

    private val tokens = listOf(
        TokenBalance(
            token = btc,
            balance = BigDecimal("0.5"),
            priceUsd = 100.0,
            changePercent24h = 1.0,
        ),
    )

    private fun overview(unreachable: Set<String> = emptySet()) = MainOverview(
        walletId = "w1",
        balances = tokens,
        totalUsd = 50.0,
        unreachableNetworks = unreachable,
    )

    @Test
    fun `onWalletActivated sets name and resets balances to Loading`() {
        val initial = MainState(
            walletName = "Old",
            balances = BalancesState.Ready(totalUsd = 1.0, tokens = emptyList(), isStale = false),
        )

        val result = MainReducer.onWalletActivated(initial, walletName = "New")

        assertEquals("New", result.walletName)
        assertEquals(BalancesState.Loading, result.balances)
    }

    @Test
    fun `onWalletNameChanged returns same state when name unchanged`() {
        val initial = MainState(walletName = "Same")

        val result = MainReducer.onWalletNameChanged(initial, "Same")

        assertSame(initial, result)
    }

    @Test
    fun `onWalletNameChanged copies state when name differs`() {
        val initial = MainState(walletName = "Old")

        val result = MainReducer.onWalletNameChanged(initial, "New")

        assertEquals("New", result.walletName)
    }

    @Test
    fun `onOverview with fresh fully-reachable data clears banner and sets Ready`() {
        val initial = MainState(banner = Banner.GenericError)
        val event = MainOverviewEvent.Data(overview = overview(), isCached = false)

        val result = MainReducer.onOverview(initial, event)

        val balances = result.balances as BalancesState.Ready
        assertEquals(50.0, balances.totalUsd, 0.0)
        assertEquals(tokens, balances.tokens)
        assertTrue(!balances.isStale)
        assertNull(result.banner)
    }

    @Test
    fun `onOverview marks state stale when data is cached`() {
        val initial = MainState(banner = Banner.GenericError)
        val event = MainOverviewEvent.Data(overview = overview(), isCached = true)

        val result = MainReducer.onOverview(initial, event)

        val balances = result.balances as BalancesState.Ready
        assertTrue(balances.isStale)
        assertEquals(Banner.GenericError, result.banner)
    }

    @Test
    fun `onOverview surfaces NetworksUnavailable banner when networks fail`() {
        val event = MainOverviewEvent.Data(
            overview = overview(unreachable = setOf("ethereum")),
            isCached = false,
        )

        val result = MainReducer.onOverview(MainState(), event)

        val banner = result.banner as Banner.NetworksUnavailable
        assertEquals(setOf("ethereum"), banner.networks)
        assertTrue((result.balances as BalancesState.Ready).isStale)
    }

    @Test
    fun `onLoadFailed sets GenericError banner`() {
        val result = MainReducer.onLoadFailed(MainState())

        assertEquals(Banner.GenericError, result.banner)
    }

    @Test
    fun `onRefreshStarted toggles isRefreshing on`() {
        val result = MainReducer.onRefreshStarted(MainState(isRefreshing = false))

        assertTrue(result.isRefreshing)
    }

    @Test
    fun `onRefreshSucceeded clears banner when everything reachable`() {
        val initial = MainState(banner = Banner.GenericError, isRefreshing = true)

        val result = MainReducer.onRefreshSucceeded(initial, overview())

        assertTrue(!result.isRefreshing)
        assertNull(result.banner)
        val balances = result.balances as BalancesState.Ready
        assertTrue(!balances.isStale)
    }

    @Test
    fun `onRefreshSucceeded sets NetworksUnavailable when some networks fail`() {
        val initial = MainState(isRefreshing = true)

        val result = MainReducer.onRefreshSucceeded(initial, overview(unreachable = setOf("btc")))

        val banner = result.banner as Banner.NetworksUnavailable
        assertEquals(setOf("btc"), banner.networks)
    }

    @Test
    fun `onRefreshFailed clears refreshing and sets GenericError`() {
        val initial = MainState(isRefreshing = true)

        val result = MainReducer.onRefreshFailed(initial)

        assertTrue(!result.isRefreshing)
        assertEquals(Banner.GenericError, result.banner)
    }
}
