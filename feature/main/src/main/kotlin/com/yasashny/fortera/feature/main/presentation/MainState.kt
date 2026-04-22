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

/** Balances section of the screen — a sealed hierarchy makes the shimmer / content split explicit. */
@Immutable
sealed interface BalancesState {

    /** First-time load, no cached data yet. Show shimmer. */
    data object Loading : BalancesState

    /** Balances available. [isStale] signals cached/partial data that should render pulsed. */
    data class Ready(
        val totalUsd: Double,
        val tokens: List<TokenBalance>,
        val isStale: Boolean,
    ) : BalancesState
}

/** Transient banner shown at the top of the screen. Part of state so it survives configuration changes. */
@Immutable
sealed interface Banner {
    data object GenericError : Banner
    data class NetworksUnavailable(val networks: Set<String>) : Banner
}
