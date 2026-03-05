package com.yasashny.fortera.feature.receive.selecttoken

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

internal object SelectTokenContract {

    data class State(
        val tokens: List<TokenDefinition> = emptyList(),
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class SelectToken(val tokenId: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToReceive(val tokenId: String) : Effect
    }
}
