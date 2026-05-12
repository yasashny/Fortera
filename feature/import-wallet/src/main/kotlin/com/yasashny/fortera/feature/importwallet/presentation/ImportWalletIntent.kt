package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface ImportWalletIntent : UiIntent {
    data class NameChanged(val value: String) : ImportWalletIntent
    data class SeedPhraseChanged(val value: String) : ImportWalletIntent
    data object ImportClicked : ImportWalletIntent
    data object BackClicked : ImportWalletIntent
    data object DismissError : ImportWalletIntent
}
