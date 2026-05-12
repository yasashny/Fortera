package com.yasashny.fortera.feature.receive.receive.presentation

import com.yasashny.fortera.core.mvi.UiState

internal data class ReceiveState(
    val tokenName: String = "",
    val tokenSymbol: String = "",
    val tokenIconUrl: String = "",
    val networkName: String = "",
    val networkIconUrl: String? = null,
    val address: String = "",
    val isLoading: Boolean = true,
) : UiState
