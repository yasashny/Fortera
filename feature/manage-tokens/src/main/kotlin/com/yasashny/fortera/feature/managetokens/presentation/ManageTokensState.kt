package com.yasashny.fortera.feature.managetokens.presentation

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.mvi.UiState

data class ManageTokensState(
    val tokens: List<TokenItem> = emptyList(),
    val isLoading: Boolean = true,
) : UiState

data class TokenItem(
    val token: TokenDefinition,
    val isEnabled: Boolean,
)
