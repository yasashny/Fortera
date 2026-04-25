package com.yasashny.fortera.feature.main.presentation

import androidx.compose.runtime.Immutable
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.mvi.UiState

@Immutable
data class MainState(
    val walletName: String? = null,
    val balances: BalancesState = BalancesState.Loading,
    val isRefreshing: Boolean = false,
    val banner: Banner? = null,
    val isWalletSelectorVisible: Boolean = false,
) : UiState {

    companion object {
        val Initial = MainState()
    }
}

@Immutable
sealed interface BalancesState {

    data object Loading : BalancesState

    data class Ready(
        val totalUsd: Double,
        val tokens: List<TokenBalance>,
        val isStale: Boolean,
    ) : BalancesState
}

@Immutable
sealed interface Banner {
    data object GenericError : Banner
    data class NetworksUnavailable(val networks: Set<String>) : Banner
}
