package com.yasashny.fortera.feature.managetokens

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object ManageTokensContract {

    data class TokenItem(
        val token: TokenDefinition,
        val isEnabled: Boolean,
    )

    data class State(
        val tokens: List<TokenItem> = emptyList(),
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Toggle(val tokenId: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
    }
}
