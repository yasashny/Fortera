package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.domain.wallet.SeedPhrase
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel

class CreateWalletViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<CreateWalletState, CreateWalletIntent, CreateWalletEffect>(CreateWalletState.Loading) {

    init {
        onIntent(CreateWalletIntent.GenerateWallet)
    }

    override fun handleIntent(intent: CreateWalletIntent) {
        when (intent) {
            CreateWalletIntent.GenerateWallet -> generateWallet()
            is CreateWalletIntent.CreateClicked -> createWallet(intent.nameTemplate)
            CreateWalletIntent.BackClicked -> sendEffect(CreateWalletEffect.NavigateBack)
        }
    }

    private fun generateWallet() = intent {
        reduce(CreateWalletState.Loading)
        walletInteractor.generateSeedPhrase()
            .onSuccess { seedPhrase ->
                reduce(CreateWalletState.ShowSeedPhrase(seedPhrase = seedPhrase.words))
            }
            .onFailure { error ->
                sendEffect(CreateWalletEffect.ShowError(error.message ?: "Failed to generate seed phrase"))
                sendEffect(CreateWalletEffect.NavigateBack)
            }
    }

    private fun createWallet(nameTemplate: String) = intent {
        withState<CreateWalletState.ShowSeedPhrase> { showState ->
            reduce(showState.copy(isCreating = true))
            val count = walletInteractor.getWalletCount().getOrElse { 0 }
            val name = nameTemplate.format(count + 1)
            val seedPhrase = SeedPhrase(showState.seedPhrase)
            walletInteractor.createWallet(name, seedPhrase)
                .onSuccess { sendEffect(CreateWalletEffect.NavigateToMain) }
                .onFailure { error ->
                    reduce(showState.copy(isCreating = false))
                    sendEffect(CreateWalletEffect.ShowError(error.message ?: "Failed to create wallet"))
                }
        }
    }
}
