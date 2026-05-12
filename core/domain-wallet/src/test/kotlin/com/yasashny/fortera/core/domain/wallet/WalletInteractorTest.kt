package com.yasashny.fortera.core.domain.wallet

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletInteractorTest {

    private val repository: WalletRepository = mockk(relaxUnitFun = true)

    /** Canonical BIP39 12-word test phrase. */
    private val validSeed =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    private fun runWithInteractor(block: suspend (WalletInteractor) -> Unit) = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        block(WalletInteractor(repository, dispatcher))
    }

    // --- pass-through Flows ---

    @Test
    fun `getWallets forwards the repository flow`() = runTest {
        every { repository.getWallets() } returns
            flowOf(listOf(Wallet("w1", "A", 0L), Wallet("w2", "B", 0L)))
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = WalletInteractor(repository, dispatcher)

        val collected = interactor.getWallets().toList()

        assertEquals(1, collected.size)
        assertEquals(2, collected.single().size)
    }

    @Test
    fun `observeActiveWallet forwards the repository flow`() = runTest {
        val expected = Wallet("w1", "A", 0L)
        every { repository.observeActiveWallet() } returns flowOf(expected)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = WalletInteractor(repository, dispatcher)

        val collected = interactor.observeActiveWallet().toList()

        assertEquals(listOf(expected), collected)
    }

    // --- suspend pass-throughs wrap in Result ---

    @Test
    fun `setActiveWallet returns success and forwards the id`() = runWithInteractor { interactor ->
        coEvery { repository.setActiveWallet("w1") } returns Unit

        val result = interactor.setActiveWallet("w1")

        assertTrue(result.isSuccess)
        coVerify { repository.setActiveWallet("w1") }
    }

    @Test
    fun `setActiveWallet wraps repository exceptions in Result_failure`() = runWithInteractor { interactor ->
        coEvery { repository.setActiveWallet(any()) } throws IllegalStateException("nope")

        val result = interactor.setActiveWallet("w1")

        assertTrue(result.isFailure)
        assertEquals("nope", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getWalletCount returns repository value`() = runWithInteractor { interactor ->
        coEvery { repository.getWalletCount() } returns 7

        assertEquals(7, interactor.getWalletCount().getOrThrow())
    }

    @Test
    fun `getSeedPhrase forwards to repository`() = runWithInteractor { interactor ->
        val seed = SeedPhrase(listOf("a", "b"))
        coEvery { repository.getSeedPhrase("w1") } returns seed

        assertEquals(seed, interactor.getSeedPhrase("w1").getOrThrow())
    }

    @Test
    fun `deleteWallet forwards id and returns success`() = runWithInteractor { interactor ->
        coEvery { repository.deleteWallet("w1") } returns Unit

        val result = interactor.deleteWallet("w1")

        assertTrue(result.isSuccess)
        coVerify { repository.deleteWallet("w1") }
    }

    // --- generateSeedPhrase ---

    @Test
    fun `generateSeedPhrase returns 12 words`() = runWithInteractor { interactor ->
        val result = interactor.generateSeedPhrase()

        val phrase = result.getOrThrow()
        assertEquals(12, phrase.wordCount)
        assertTrue(phrase.words.all { it.isNotBlank() })
    }

    // --- validateSeedPhrase ---

    @Test
    fun `validateSeedPhrase fails when input is blank`() = runWithInteractor { interactor ->
        val result = interactor.validateSeedPhrase("   ")

        assertTrue(result.isFailure)
        assertEquals("Seed phrase cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateSeedPhrase fails when word count is unsupported`() = runWithInteractor { interactor ->
        val result = interactor.validateSeedPhrase("abandon abandon abandon")

        assertTrue(result.isFailure)
        assertEquals("Seed phrase must contain 12 or 24 words", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateSeedPhrase fails when one or more words are not in the wordlist`() =
        runWithInteractor { interactor ->
            val input = (1..11).joinToString(" ") { "abandon" } + " notawordatall"

            val result = interactor.validateSeedPhrase(input)

            assertTrue(result.isFailure)
            assertEquals("Seed phrase contains invalid words", result.exceptionOrNull()?.message)
        }

    @Test
    fun `validateSeedPhrase fails on a valid wordlist with bad checksum`() = runWithInteractor { interactor ->
        // All "abandon" words from the BIP39 wordlist but wrong final word for the checksum.
        val result = interactor.validateSeedPhrase((1..12).joinToString(" ") { "abandon" })

        assertTrue(result.isFailure)
        assertEquals("Seed phrase checksum is invalid", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateSeedPhrase succeeds for a canonical 12-word phrase`() = runWithInteractor { interactor ->
        val result = interactor.validateSeedPhrase(validSeed)

        val phrase = result.getOrThrow()
        assertEquals(12, phrase.wordCount)
        assertEquals("about", phrase.words.last())
    }

    @Test
    fun `validateSeedPhrase trims surrounding whitespace`() = runWithInteractor { interactor ->
        val result = interactor.validateSeedPhrase("  $validSeed  ")

        assertTrue(result.isSuccess)
    }

    // --- verifySeedPhraseOrder ---

    @Test
    fun `verifySeedPhraseOrder succeeds when selected words match the required indices`() =
        runWithInteractor { interactor ->
            val seed = SeedPhrase(listOf("one", "two", "three", "four", "five"))

            val result = interactor.verifySeedPhraseOrder(
                seedPhrase = seed,
                requiredIndices = listOf(0, 2, 4),
                selectedWords = listOf("one", "three", "five"),
            )

            assertTrue(result.isSuccess)
        }

    @Test
    fun `verifySeedPhraseOrder fails when selected words are in the wrong order`() =
        runWithInteractor { interactor ->
            val seed = SeedPhrase(listOf("one", "two", "three"))

            val result = interactor.verifySeedPhraseOrder(
                seedPhrase = seed,
                requiredIndices = listOf(0, 2),
                selectedWords = listOf("three", "one"),
            )

            assertTrue(result.isFailure)
            assertEquals(
                "Words are in wrong order. Please try again.",
                result.exceptionOrNull()?.message,
            )
        }

    @Test
    fun `verifySeedPhraseOrder fails when a required index is out of bounds`() =
        runWithInteractor { interactor ->
            val seed = SeedPhrase(listOf("one", "two"))

            val result = interactor.verifySeedPhraseOrder(
                seedPhrase = seed,
                requiredIndices = listOf(7),
                selectedWords = listOf("anything"),
            )

            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull()?.message)
        }

    // --- createWallet / importWallet ---

    @Test
    fun `createWallet forwards name and seed to the repository`() = runWithInteractor { interactor ->
        val seed = SeedPhrase(listOf("a"))
        val created = Wallet("w1", "Name", 0L)
        coEvery { repository.createWallet("Name", seed) } returns created

        val result = interactor.createWallet("Name", seed)

        assertEquals(created, result.getOrThrow())
        coVerify { repository.createWallet("Name", seed) }
    }

    @Test
    fun `importWallet parses the seed before forwarding to the repository`() =
        runWithInteractor { interactor ->
            val expected = Wallet("w1", "Name", 0L)
            coEvery { repository.importWallet(eq("Name"), any()) } returns expected

            val result = interactor.importWallet("Name", validSeed)

            assertEquals(expected, result.getOrThrow())
            // MockK's `match { }` doesn't unwrap @JvmInline value classes, so verify only the
            // call shape — `importWallet` reaching the repo at all means parseSeedPhrase succeeded.
            coVerify(exactly = 1) { repository.importWallet("Name", any()) }
        }

    @Test
    fun `importWallet fails for an invalid seed without touching the repository`() =
        runWithInteractor { interactor ->
            val result = interactor.importWallet("Name", "bogus seed")

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { repository.importWallet(any(), any()) }
        }

    // --- updateWalletName ---

    @Test
    fun `updateWalletName fails when name is blank`() = runWithInteractor { interactor ->
        val result = interactor.updateWalletName("w1", "   ")

        assertTrue(result.isFailure)
        assertEquals("Wallet name cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.updateWalletName(any(), any()) }
    }

    @Test
    fun `updateWalletName forwards to repository for non-blank names`() = runWithInteractor { interactor ->
        coEvery { repository.updateWalletName("w1", "New") } returns Unit

        val result = interactor.updateWalletName("w1", "New")

        assertTrue(result.isSuccess)
        coVerify { repository.updateWalletName("w1", "New") }
    }

    @Test
    fun `updateWalletName surfaces repository failure as Result_failure`() =
        runWithInteractor { interactor ->
            coEvery { repository.updateWalletName(any(), any()) } throws RuntimeException("db")

            val result = interactor.updateWalletName("w1", "New")

            assertTrue(result.isFailure)
            assertEquals("db", result.exceptionOrNull()?.message)
        }

    @Test
    fun `getSeedPhrase returns success with null when repository has no entry`() =
        runWithInteractor { interactor ->
            coEvery { repository.getSeedPhrase("missing") } returns null

            val result = interactor.getSeedPhrase("missing")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }
}
