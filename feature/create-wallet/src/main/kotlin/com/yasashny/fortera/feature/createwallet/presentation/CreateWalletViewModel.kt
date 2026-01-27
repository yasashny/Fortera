package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.domain.wallet.SeedPhrase
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel

class CreateWalletViewModel(
    private val walletInteractor: WalletInteractor
) : MviViewModel<CreateWalletContract.State, CreateWalletContract.Intent, CreateWalletContract.Effect>(
    CreateWalletContract.State.Loading
) {

    init {
        onIntent(CreateWalletContract.Intent.GenerateWallet)
    }

    override fun handleIntent(intent: CreateWalletContract.Intent) {
        when (intent) {
            CreateWalletContract.Intent.GenerateWallet -> generateWallet()
            is CreateWalletContract.Intent.CreateClicked -> createWallet(intent.nameTemplate)
            CreateWalletContract.Intent.BackClicked -> intent { sendEffect(CreateWalletContract.Effect.NavigateBack) }
        }
    }

    private fun generateWallet() = intent {
        reduce(CreateWalletContract.State.Loading)
        walletInteractor.generateSeedPhrase()
            .onSuccess { seedPhrase ->
                reduce(CreateWalletContract.State.ShowSeedPhrase(seedPhrase = seedPhrase.words))
            }
            .onFailure { error ->
                sendEffect(
                    CreateWalletContract.Effect.ShowError(
                        error.message ?: "Failed to generate seed phrase"
                    )
                )
                sendEffect(CreateWalletContract.Effect.NavigateBack)
            }
    }

    private fun createWallet(nameTemplate: String) = intent {
        withState<CreateWalletContract.State.ShowSeedPhrase> { showState ->
            reduce(showState.copy(isCreating = true))
            val count = walletInteractor.getWalletCount().getOrElse { 0 }
            val name = nameTemplate.format(count + 1)
            val seedPhrase = SeedPhrase(showState.seedPhrase)
            walletInteractor.createWallet(name, seedPhrase)
                .onSuccess { sendEffect(CreateWalletContract.Effect.NavigateToMain) }
                .onFailure { error ->
                    reduce(showState.copy(isCreating = false))
                    sendEffect(
                        CreateWalletContract.Effect.ShowError(
                            error.message ?: "Failed to create wallet"
                        )
                    )
                }
        }
    }
}
