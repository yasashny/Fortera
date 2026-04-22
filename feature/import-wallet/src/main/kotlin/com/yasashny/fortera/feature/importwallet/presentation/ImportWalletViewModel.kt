package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.importwallet.R
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.Effect
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.Intent
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.State

class ImportWalletViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<State, Intent, Effect>(State.Content()) {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.NameChanged -> onNameChanged(intent.value)
            is Intent.SeedPhraseChanged -> onSeedPhraseChanged(intent.value)
            Intent.ImportClicked -> onImportClicked()
            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
            Intent.DismissError -> clearError()
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

    private fun clearError() {
        updateState { state ->
            (state as? State.Content)?.copy(errorMessage = null) ?: state
        }
    }

    private fun onImportClicked() = intent {
        withState<State.Content> { content ->
            val nameError = if (content.name.isBlank())
                UiText.of(R.string.import_wallet_error_name_empty) else null
            val seedError = if (content.seedPhrase.isBlank())
                UiText.of(R.string.import_wallet_error_seed_empty) else null
            if (nameError != null || seedError != null) {
                reduce(content.copy(nameError = nameError, seedPhraseError = seedError))
                return@withState
            }
            reduce(content.copy(isLoading = true, nameError = null, seedPhraseError = null))
            walletInteractor.importWallet(content.name, content.seedPhrase)
                .onSuccess {
                    reduce(content.copy(isLoading = false))
                    sendEffect(Effect.NavigateToHome)
                }
                .onFailure { error ->
                    val message = error.message?.takeIf { it.isNotBlank() }?.let(UiText::of)
                        ?: UiText.of(R.string.import_wallet_error_import_failed)
                    reduce(content.copy(isLoading = false, errorMessage = message))
                }
        }
    }
}
