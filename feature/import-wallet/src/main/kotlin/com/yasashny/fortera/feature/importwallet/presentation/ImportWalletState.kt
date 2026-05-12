package com.yasashny.fortera.feature.importwallet.presentation

import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

sealed interface ImportWalletState : UiState {
    val errorMessage: UiText?

    data class Content(
        val name: String = "",
        val seedPhrase: String = "",
        val isLoading: Boolean = false,
        val nameError: UiText? = null,
        val seedPhraseError: UiText? = null,
        override val errorMessage: UiText? = null,
    ) : ImportWalletState
}
