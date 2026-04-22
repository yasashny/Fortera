package com.yasashny.fortera.core.domain.wallet

import cash.z.ecc.android.bip39.Mnemonics
import com.yasashny.fortera.core.common.Interactor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

class WalletInteractor(
    private val walletRepository: WalletRepository,
    dispatcher: CoroutineDispatcher,
) : Interactor(dispatcher) {

    fun getWallets(): Flow<List<Wallet>> =
        executeFlow { walletRepository.getWallets() }

    fun observeActiveWallet(): Flow<Wallet?> =
        executeFlow { walletRepository.observeActiveWallet() }

    suspend fun setActiveWallet(walletId: String): Result<Unit> = execute {
        walletRepository.setActiveWallet(walletId)
    }

    suspend fun getWalletCount(): Result<Int> = execute {
        walletRepository.getWalletCount()
    }

    suspend fun generateSeedPhrase(): Result<SeedPhrase> = execute {
        val code = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)
        SeedPhrase(code.words.map { String(it) })
    }

    suspend fun validateSeedPhrase(input: String): Result<SeedPhrase> = execute {
        parseSeedPhrase(input)
    }

    suspend fun verifySeedPhraseOrder(
        seedPhrase: SeedPhrase,
        requiredIndices: List<Int>,
        selectedWords: List<String>,
    ): Result<Unit> = execute {
        val expected = requiredIndices.map { index ->
            seedPhrase.words.getOrNull(index) ?: error("Invalid word index: $index")
        }
        if (selectedWords != expected) {
            error("Words are in wrong order. Please try again.")
        }
    }

    suspend fun createWallet(name: String, seedPhrase: SeedPhrase): Result<Wallet> = execute {
        walletRepository.createWallet(name, seedPhrase)
    }

    suspend fun importWallet(name: String, seedPhraseInput: String): Result<Wallet> = execute {
        walletRepository.importWallet(name, parseSeedPhrase(seedPhraseInput))
    }

    suspend fun updateWalletName(id: String, name: String): Result<Unit> = execute {
        if (name.isBlank()) error("Wallet name cannot be empty")
        walletRepository.updateWalletName(id, name)
    }

    suspend fun deleteWallet(id: String): Result<Unit> = execute {
        walletRepository.deleteWallet(id)
    }

    suspend fun getSeedPhrase(walletId: String): Result<SeedPhrase?> = execute {
        walletRepository.getSeedPhrase(walletId)
    }

    private fun parseSeedPhrase(input: String): SeedPhrase {
        val trimmed = input.trim()
        if (trimmed.isBlank()) error("Seed phrase cannot be empty")
        try {
            val code = Mnemonics.MnemonicCode(trimmed.toCharArray())
            code.validate()
            return SeedPhrase(code.words.map { String(it) })
        } catch (e: Mnemonics.WordCountException) {
            error("Seed phrase must contain 12 or 24 words")
        } catch (e: Mnemonics.InvalidWordException) {
            error("Seed phrase contains invalid words")
        } catch (e: Mnemonics.ChecksumException) {
            error("Seed phrase checksum is invalid")
        }
    }
}
