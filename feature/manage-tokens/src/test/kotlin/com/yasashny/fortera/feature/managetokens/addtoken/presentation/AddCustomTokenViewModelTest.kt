package com.yasashny.fortera.feature.managetokens.addtoken.presentation

import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.TokenMetadataFetcher
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCustomTokenViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val tokenRepository: TokenRepository = mockk(relaxUnitFun = true)
    private val fetcher: TokenMetadataFetcher = mockk()
    private val walletInteractor: WalletInteractor = mockk()

    private val validAddress = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"
    private val activeWallet = Wallet(id = "w1", name = "Main", createdAt = 0L)

    private val metadata = CustomTokenMetadata(
        name = "USD Coin",
        symbol = "USDC",
        decimals = 6,
        contractAddress = validAddress,
        network = BlockchainNetwork.ETHEREUM,
        coingeckoId = "usd-coin",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { walletInteractor.observeActiveWallet() } returns flowOf(activeWallet)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and idle`() {
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        assertEquals("", viewModel.currentState.input)
        assertEquals(Verification.Idle, viewModel.currentState.verification)
        assertFalse(viewModel.currentState.isAdding)
        assertFalse(viewModel.currentState.canAdd)
    }

    @Test
    fun `short invalid input stays Idle`() = runTest(dispatcher) {
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.InputChanged("0x1"))
        advanceUntilIdle()

        assertEquals(Verification.Idle, viewModel.currentState.verification)
    }

    @Test
    fun `long invalid input switches to Failed`() = runTest(dispatcher) {
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.InputChanged("0xnotanaddressreally"))
        advanceUntilIdle()

        assertTrue(viewModel.currentState.verification is Verification.Failed)
    }

    @Test
    fun `valid address shows InProgress before debounce, Verified after`() = runTest(dispatcher) {
        coEvery { fetcher.fetch(validAddress) } returns Result.success(metadata)
        coEvery { tokenRepository.findTokenByContract(validAddress) } returns null
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.InputChanged(validAddress))
        runCurrent()

        assertEquals(Verification.InProgress, viewModel.currentState.verification)

        advanceTimeBy(400)
        advanceUntilIdle()

        val v = viewModel.currentState.verification as Verification.Verified
        assertEquals(metadata, v.metadata)
        assertFalse(v.alreadyAdded)
        assertTrue(viewModel.currentState.canAdd)
    }

    @Test
    fun `Verified for an existing token marks alreadyAdded`() = runTest(dispatcher) {
        val existing = TokenDefinition(
            id = "usd-coin",
            name = "USD Coin",
            symbol = "USDC",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 6,
            contractAddress = validAddress,
            coingeckoId = "usd-coin",
            isDefault = true,
        )
        coEvery { fetcher.fetch(validAddress) } returns Result.success(metadata)
        coEvery { tokenRepository.findTokenByContract(validAddress) } returns existing
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.InputChanged(validAddress))
        advanceUntilIdle()

        val v = viewModel.currentState.verification as Verification.Verified
        assertTrue(v.alreadyAdded)
    }

    @Test
    fun `debounce is reset by subsequent input changes`() = runTest(dispatcher) {
        coEvery { fetcher.fetch(validAddress) } returns Result.success(metadata)
        coEvery { tokenRepository.findTokenByContract(any()) } returns null
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.InputChanged(validAddress))
        advanceTimeBy(200)
        viewModel.onIntent(AddCustomTokenIntent.InputChanged("0xnotanaddress1234"))
        advanceUntilIdle()

        // First input's pending verification job is cancelled before completing,
        // so we land on the second (invalid) input's Failed state, not Verified.
        assertTrue(viewModel.currentState.verification is Verification.Failed)
    }

    @Test
    fun `fetch failure transitions to Failed`() = runTest(dispatcher) {
        coEvery { fetcher.fetch(validAddress) } returns Result.failure(RuntimeException("404"))
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.InputChanged(validAddress))
        advanceUntilIdle()

        assertTrue(viewModel.currentState.verification is Verification.Failed)
    }

    @Test
    fun `AddClicked is no-op when not verified`() = runTest(dispatcher) {
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)

        viewModel.onIntent(AddCustomTokenIntent.AddClicked)
        advanceUntilIdle()

        coVerify(exactly = 0) { tokenRepository.addCustomToken(any()) }
        coVerify(exactly = 0) { tokenRepository.setTokenEnabled(any(), any(), any()) }
    }

    @Test
    fun `AddClicked adds new custom token, enables it and emits NavigateBack`() =
        runTest(dispatcher) {
            coEvery { fetcher.fetch(validAddress) } returns Result.success(metadata)
            coEvery { tokenRepository.findTokenByContract(validAddress) } returns null
            val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)
            viewModel.onIntent(AddCustomTokenIntent.InputChanged(validAddress))
            advanceUntilIdle()

            viewModel.onIntent(AddCustomTokenIntent.AddClicked)
            advanceUntilIdle()

            coVerify {
                tokenRepository.addCustomToken(match {
                    it.name == "USD Coin" && it.contractAddress == validAddress
                })
                tokenRepository.setTokenEnabled("w1", "usd-coin", true)
            }
            assertEquals(AddCustomTokenEffect.NavigateBack, viewModel.effect.first())
        }

    @Test
    fun `AddClicked uses existing token when alreadyAdded`() = runTest(dispatcher) {
        val existing = TokenDefinition(
            id = "usd-coin",
            name = "USD Coin",
            symbol = "USDC",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 6,
            contractAddress = validAddress,
            coingeckoId = "usd-coin",
            isDefault = true,
        )
        coEvery { fetcher.fetch(validAddress) } returns Result.success(metadata)
        coEvery { tokenRepository.findTokenByContract(validAddress) } returns existing
        val viewModel = AddCustomTokenViewModel(tokenRepository, fetcher, walletInteractor)
        viewModel.onIntent(AddCustomTokenIntent.InputChanged(validAddress))
        advanceUntilIdle()

        viewModel.onIntent(AddCustomTokenIntent.AddClicked)
        advanceUntilIdle()

        coVerify(exactly = 0) { tokenRepository.addCustomToken(any()) }
        coVerify { tokenRepository.setTokenEnabled("w1", "usd-coin", true) }
    }
}
