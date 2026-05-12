package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.SeedPhrase
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class WalletTransactionSenderImplTest {

    private val walletInteractor: WalletInteractor = mockk()
    private val addresses: WalletAddressesService = mockk()
    private val sendRepo: SendTransactionRepository = mockk()
    private val sender = WalletTransactionSenderImpl(walletInteractor, addresses, sendRepo)

    private val ethToken = TokenDefinition(
        id = "ethereum",
        name = "Ethereum",
        symbol = "ETH",
        network = BlockchainNetwork.ETHEREUM,
        decimals = 18,
        contractAddress = null,
        coingeckoId = "ethereum",
        isDefault = true,
    )
    private val btcToken = TokenDefinition(
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
    fun `estimateFees uses ETH address for ETHEREUM tokens`() = runTest {
        coEvery { addresses.forWallet("w1") } returns WalletAddresses(eth = "0xeth", btc = "btc")
        coEvery {
            sendRepo.estimateFees(ethToken, "0xeth", BigDecimal("1"))
        } returns Result.success(emptyMap())

        val result = sender.estimateFees("w1", ethToken, BigDecimal("1"))

        assertTrue(result.isSuccess)
        coVerify { sendRepo.estimateFees(ethToken, "0xeth", BigDecimal("1")) }
    }

    @Test
    fun `estimateFees uses BTC address for BITCOIN tokens`() = runTest {
        coEvery { addresses.forWallet("w1") } returns WalletAddresses(eth = "0xeth", btc = "btc")
        coEvery {
            sendRepo.estimateFees(btcToken, "btc", BigDecimal("0.5"))
        } returns Result.success(emptyMap())

        val result = sender.estimateFees("w1", btcToken, BigDecimal("0.5"))

        assertTrue(result.isSuccess)
        coVerify { sendRepo.estimateFees(btcToken, "btc", BigDecimal("0.5")) }
    }

    @Test
    fun `estimateFees passes null address when addresses are unavailable`() = runTest {
        coEvery { addresses.forWallet("w1") } returns null
        coEvery {
            sendRepo.estimateFees(ethToken, null, BigDecimal.ONE)
        } returns Result.success(emptyMap())

        sender.estimateFees("w1", ethToken, BigDecimal.ONE)

        coVerify { sendRepo.estimateFees(ethToken, null, BigDecimal.ONE) }
    }

    @Test
    fun `send fails fast when seed phrase is unavailable`() = runTest {
        coEvery { walletInteractor.getSeedPhrase("w1") } returns Result.success(null)

        val result = sender.send("w1", ethToken, "0xto", BigDecimal.ONE, FeeSpeed.FAST)

        assertTrue(result.isFailure)
        assertEquals(
            "Seed phrase unavailable for wallet w1",
            result.exceptionOrNull()?.message,
        )
        coVerify(exactly = 0) { sendRepo.send(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `send forwards seed and parameters to the repository`() = runTest {
        val seed = SeedPhrase(listOf("alpha", "beta"))
        coEvery { walletInteractor.getSeedPhrase("w1") } returns Result.success(seed)
        coEvery {
            sendRepo.send(seed.toDisplayString(), ethToken, "0xto", BigDecimal.ONE, FeeSpeed.FAST)
        } returns Result.success("0xtxhash")

        val result = sender.send("w1", ethToken, "0xto", BigDecimal.ONE, FeeSpeed.FAST)

        assertEquals("0xtxhash", result.getOrThrow())
        coVerify {
            sendRepo.send("alpha beta", ethToken, "0xto", BigDecimal.ONE, FeeSpeed.FAST)
        }
    }
}
