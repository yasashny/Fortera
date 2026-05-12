package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.BalanceResult
import com.yasashny.fortera.core.domaincrypto.repository.FetchPolicy
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class WalletBalancesImplTest {

    private val walletInteractor: WalletInteractor = mockk(relaxed = true)
    private val tokenRepo: TokenRepository = mockk()
    private val balanceRepo: BalanceRepository = mockk()
    private val addresses: WalletAddressesService = mockk()

    private val walletId = "w1"
    private val enabledIds = setOf("bitcoin")
    private val walletAddresses = WalletAddresses(eth = "0xeth", btc = "btc")

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
    private val cachedBalance = TokenBalance(
        token = btc,
        balance = BigDecimal("0.1"),
        priceUsd = 100.0,
        changePercent24h = 0.0,
    )
    private val freshBalance = TokenBalance(
        token = btc,
        balance = BigDecimal("0.5"),
        priceUsd = 100.0,
        changePercent24h = 0.0,
    )

    private fun build() = WalletBalancesImpl(
        walletInteractor = walletInteractor,
        tokenRepository = tokenRepo,
        balanceRepository = balanceRepo,
        addresses = addresses,
        dispatcher = UnconfinedTestDispatcher(),
    )

    private fun stubAddresses() {
        coEvery { addresses.forWallet(walletId) } returns walletAddresses
    }

    private fun stubFreshSuccess(failed: Set<String> = emptySet()) {
        coEvery {
            balanceRepo.getTokenBalances(
                walletId = walletId,
                ethAddress = "0xeth",
                btcAddress = "btc",
                enabledTokenIds = enabledIds,
                policy = any(),
            )
        } returns Result.success(BalanceResult(balances = listOf(freshBalance), failedNetworks = failed))
    }

    private fun stubFreshFailure() {
        coEvery {
            balanceRepo.getTokenBalances(
                walletId = walletId,
                ethAddress = "0xeth",
                btcAddress = "btc",
                enabledTokenIds = enabledIds,
                policy = any(),
            )
        } returns Result.failure(RuntimeException("offline"))
    }

    // --- observe ---

    @Test
    fun `observe emits cached snapshot then fresh snapshot when both are available`() = runTest {
        every { tokenRepo.observeEnabledTokenIds(walletId) } returns flowOf(enabledIds)
        coEvery { balanceRepo.getCachedBalances(walletId, enabledIds) } returns listOf(cachedBalance)
        stubAddresses()
        stubFreshSuccess()

        val events = build().observe(walletId).toList()

        assertEquals(2, events.size)
        val first = events[0] as WalletBalancesEvent.Snapshot
        assertTrue(first.value.isFromCache)
        assertEquals(listOf(cachedBalance), first.value.balances)
        val second = events[1] as WalletBalancesEvent.Snapshot
        assertTrue(!second.value.isFromCache)
        assertEquals(listOf(freshBalance), second.value.balances)
    }

    @Test
    fun `observe emits Loading and then Snapshot when no cache is available`() = runTest {
        every { tokenRepo.observeEnabledTokenIds(walletId) } returns flowOf(enabledIds)
        coEvery { balanceRepo.getCachedBalances(walletId, enabledIds) } returns null
        stubAddresses()
        stubFreshSuccess()

        val events = build().observe(walletId).toList()

        assertEquals(2, events.size)
        assertTrue("First event was $events", events[0] is WalletBalancesEvent.Loading)
        assertTrue(events[1] is WalletBalancesEvent.Snapshot)
        val snapshot = (events[1] as WalletBalancesEvent.Snapshot).value
        assertTrue(!snapshot.isFromCache)
        assertEquals(50.0, snapshot.totalUsd, 0.0001)
    }

    @Test
    fun `observe emits Loading and then Failed when no cache and fetch fails`() = runTest {
        every { tokenRepo.observeEnabledTokenIds(walletId) } returns flowOf(enabledIds)
        coEvery { balanceRepo.getCachedBalances(walletId, enabledIds) } returns null
        stubAddresses()
        stubFreshFailure()

        val events = build().observe(walletId).toList()

        assertEquals(2, events.size)
        assertTrue(events[0] is WalletBalancesEvent.Loading)
        val failed = events[1] as WalletBalancesEvent.Failed
        assertEquals(walletId, failed.walletId)
        assertEquals("offline", failed.cause.message)
    }

    @Test
    fun `observe suppresses Failed when cached snapshot was already emitted`() = runTest {
        every { tokenRepo.observeEnabledTokenIds(walletId) } returns flowOf(enabledIds)
        coEvery { balanceRepo.getCachedBalances(walletId, enabledIds) } returns listOf(cachedBalance)
        stubAddresses()
        stubFreshFailure()

        val events = build().observe(walletId).toList()

        assertEquals(1, events.size)
        assertTrue(events.single() is WalletBalancesEvent.Snapshot)
    }

    @Test
    fun `observe surfaces failure as Failed when addresses are unavailable and no cache`() = runTest {
        every { tokenRepo.observeEnabledTokenIds(walletId) } returns flowOf(enabledIds)
        coEvery { balanceRepo.getCachedBalances(walletId, enabledIds) } returns null
        coEvery { addresses.forWallet(walletId) } returns null

        val events = build().observe(walletId).toList()

        assertTrue(events[0] is WalletBalancesEvent.Loading)
        val failed = events[1] as WalletBalancesEvent.Failed
        assertTrue(
            "Expected addresses-unavailable message, got '${failed.cause.message}'",
            failed.cause.message?.contains("Addresses unavailable") == true,
        )
    }

    @Test
    fun `observe propagates unreachable networks into the snapshot`() = runTest {
        every { tokenRepo.observeEnabledTokenIds(walletId) } returns flowOf(enabledIds)
        coEvery { balanceRepo.getCachedBalances(walletId, enabledIds) } returns null
        stubAddresses()
        stubFreshSuccess(failed = setOf("ethereum"))

        val events = build().observe(walletId).toList()

        val snapshot = (events.last() as WalletBalancesEvent.Snapshot).value
        assertEquals(setOf("ethereum"), snapshot.unreachableNetworks)
    }

    // --- refresh ---

    @Test
    fun `refresh fetches with RemoteOnly policy and returns a success snapshot`() = runTest {
        coEvery { tokenRepo.getEnabledTokenIds(walletId) } returns enabledIds
        stubAddresses()
        stubFreshSuccess()

        val result = build().refresh(walletId)

        val snapshot = result.getOrThrow()
        assertEquals(listOf(freshBalance), snapshot.balances)
        coVerify {
            balanceRepo.getTokenBalances(
                walletId = walletId,
                ethAddress = "0xeth",
                btcAddress = "btc",
                enabledTokenIds = enabledIds,
                policy = FetchPolicy.RemoteOnly,
            )
        }
    }

    @Test
    fun `refresh returns failure when balance fetch fails`() = runTest {
        coEvery { tokenRepo.getEnabledTokenIds(walletId) } returns enabledIds
        stubAddresses()
        stubFreshFailure()

        val result = build().refresh(walletId)

        assertTrue(result.isFailure)
        assertEquals("offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun `refresh returns failure when addresses are unavailable`() = runTest {
        coEvery { tokenRepo.getEnabledTokenIds(walletId) } returns enabledIds
        coEvery { addresses.forWallet(walletId) } returns null

        val result = build().refresh(walletId)

        assertTrue(result.isFailure)
    }
}
