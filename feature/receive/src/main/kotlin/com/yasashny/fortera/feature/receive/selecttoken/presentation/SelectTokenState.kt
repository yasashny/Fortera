package com.yasashny.fortera.feature.receive.selecttoken.presentation

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.mvi.UiState

internal data class SelectTokenState(
    val tokens: List<TokenDefinition> = emptyList(),
    val isLoading: Boolean = true,
) : UiState
