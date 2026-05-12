package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.importwallet.R

class ImportWalletViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<ImportWalletState, ImportWalletIntent, ImportWalletEffect>(ImportWalletState.Content()) {

    override fun handleIntent(intent: ImportWalletIntent) {
        when (intent) {
            is ImportWalletIntent.NameChanged -> onNameChanged(intent.value)
            is ImportWalletIntent.SeedPhraseChanged -> onSeedPhraseChanged(intent.value)
            ImportWalletIntent.ImportClicked -> onImportClicked()
            ImportWalletIntent.BackClicked -> sendEffect(ImportWalletEffect.NavigateBack)
            ImportWalletIntent.DismissError -> clearError()
        }
    }

    private fun onNameChanged(value: String) {
        updateState { state ->
            (state as? ImportWalletState.Content)?.copy(name = value, nameError = null) ?: state
        }
    }

    private fun onSeedPhraseChanged(value: String) {
        updateState { state ->
            (state as? ImportWalletState.Content)?.copy(seedPhrase = value, seedPhraseError = null) ?: state
        }
    }

    private fun clearError() {
        updateState { state ->
            (state as? ImportWalletState.Content)?.copy(errorMessage = null) ?: state
        }
    }

    private fun onImportClicked() = intent {
        withState<ImportWalletState.Content> { content ->
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
                    sendEffect(ImportWalletEffect.NavigateToHome)
                }
                .onFailure { error ->
                    val message = error.message?.takeIf { it.isNotBlank() }?.let(UiText::of)
                        ?: UiText.of(R.string.import_wallet_error_import_failed)
                    reduce(content.copy(isLoading = false, errorMessage = message))
                }
        }
    }
}
