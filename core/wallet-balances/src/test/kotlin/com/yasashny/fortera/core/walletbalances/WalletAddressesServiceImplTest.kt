package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.SeedPhrase
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletAddressesServiceImplTest {

    private val walletInteractor: WalletInteractor = mockk()
    private val resolver: AddressResolver = mockk()
    private val service = WalletAddressesServiceImpl(walletInteractor, resolver)

    private val seedPhrase = SeedPhrase(listOf("alpha", "beta", "gamma"))

    @Test
    fun `forWallet returns null when seed phrase is unavailable`() = runTest {
        coEvery { walletInteractor.getSeedPhrase("w1") } returns Result.success(null)

        assertNull(service.forWallet("w1"))
    }

    @Test
    fun `forWallet returns null when getSeedPhrase fails`() = runTest {
        coEvery { walletInteractor.getSeedPhrase("w1") } returns Result.failure(RuntimeException())

        assertNull(service.forWallet("w1"))
    }

    @Test
    fun `forWallet returns resolved eth and btc addresses`() = runTest {
        coEvery { walletInteractor.getSeedPhrase("w1") } returns Result.success(seedPhrase)
        every { resolver.ethAddress(seedPhrase.toDisplayString()) } returns "0xeth"
        coEvery { resolver.btcAddress(seedPhrase.toDisplayString()) } returns "btc-addr"

        val addresses = service.forWallet("w1")

        assertEquals(WalletAddresses(eth = "0xeth", btc = "btc-addr"), addresses)
    }

    @Test
    fun `forActiveWallet returns null when there is no active wallet`() = runTest {
        every { walletInteractor.observeActiveWallet() } returns flowOf(null)

        assertNull(service.forActiveWallet())
    }

    @Test
    fun `forActiveWallet resolves addresses for the active wallet id`() = runTest {
        val active = Wallet("w-active", "Main", 0L)
        every { walletInteractor.observeActiveWallet() } returns flowOf(active)
        coEvery { walletInteractor.getSeedPhrase("w-active") } returns Result.success(seedPhrase)
        every { resolver.ethAddress(any()) } returns "0xactive"
        coEvery { resolver.btcAddress(any()) } returns "bc1qactive"

        val addresses = service.forActiveWallet()

        assertEquals(WalletAddresses(eth = "0xactive", btc = "bc1qactive"), addresses)
        coVerify { walletInteractor.getSeedPhrase("w-active") }
    }
}
