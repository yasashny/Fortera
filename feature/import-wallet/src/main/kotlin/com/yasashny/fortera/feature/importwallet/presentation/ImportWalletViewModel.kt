package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.Effect
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.Intent
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.State

class ImportWalletViewModel(
    private val walletInteractor: WalletInteractor
) : MviViewModel<State, Intent, Effect>(State.Content()) {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.NameChanged -> onNameChanged(intent.value)
            is Intent.SeedPhraseChanged -> onSeedPhraseChanged(intent.value)
            Intent.PasteClicked -> onPasteClicked()
            Intent.ImportClicked -> onImportClicked()
            Intent.BackClicked -> onBackClicked()
        }
    }

    private fun onNameChanged(value: String) {
        updateState { state ->
            (state as? State.Content)?.copy(name = value, nameError = null) ?: state
        }
    }

    private fun onSeedPhraseChanged(value: String) {
        updateState { state ->
            (state as? State.Content)?.copy(seedPhrase = value, seedPhraseError = null) ?: state
        }
    }

    private fun onPasteClicked() = intent {
        sendEffect(Effect.RequestPaste)
    }

    private fun onImportClicked() = intent {
        withState<State.Content> { content ->
            val nameError = if (content.name.isBlank()) "Wallet name cannot be empty" else null
            val seedPhraseError =
                if (content.seedPhrase.isBlank()) "Seed phrase cannot be empty" else null
            if (nameError != null || seedPhraseError != null) {
                reduce(content.copy(nameError = nameError, seedPhraseError = seedPhraseError))
                return@withState
            }
            reduce(content.copy(isLoading = true, nameError = null, seedPhraseError = null))
            walletInteractor.importWallet(content.name, content.seedPhrase)
                .onSuccess {
                    reduce(content.copy(isLoading = false))
                    sendEffect(Effect.NavigateToHome)
                }
                .onFailure { error ->
                    reduce(content.copy(isLoading = false))
                    sendEffect(Effect.ShowError(error.message ?: "Import failed"))
                }
        }
    }

    private fun onBackClicked() = intent {
        sendEffect(Effect.NavigateBack)
    }
}
