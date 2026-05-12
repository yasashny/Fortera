package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.mvi.UiState

sealed interface CreateWalletState : UiState {

    data object Loading : CreateWalletState

    data class ShowSeedPhrase(
        val seedPhrase: List<String>,
        val isCreating: Boolean = false,
    ) : CreateWalletState
}
