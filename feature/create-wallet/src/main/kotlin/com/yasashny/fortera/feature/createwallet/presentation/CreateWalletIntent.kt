package com.yasashny.fortera.feature.createwallet.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface CreateWalletIntent : UiIntent {
    data object GenerateWallet : CreateWalletIntent
    data class CreateClicked(val nameTemplate: String) : CreateWalletIntent
    data object BackClicked : CreateWalletIntent
}
