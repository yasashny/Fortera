package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.feature.main.domain.MainOverview
import com.yasashny.fortera.feature.main.domain.MainOverviewEvent

/**
 * Pure state transitions — no side effects, no dispatchers. Easy to unit-test.
 *
 * Each function maps an input (event or intent payload) to a new [MainState],
 * keeping the ViewModel a thin orchestrator over these transitions.
 */
internal object MainReducer {

    fun onWalletActivated(state: MainState, walletName: String): MainState = state.copy(
        walletName = walletName,
        balances = BalancesState.Loading,
    )

    fun onWalletNameChanged(state: MainState, walletName: String): MainState =
        if (state.walletName == walletName) state else state.copy(walletName = walletName)

    fun onOverview(state: MainState, event: MainOverviewEvent.Data): MainState = state.copy(
        balances = event.overview.toBalancesState(isCached = event.isCached),
        banner = nextBannerAfterOverview(event, previous = state.banner),
    )

    fun onLoadFailed(state: MainState): MainState = state.copy(
        banner = Banner.GenericError,
    )

    fun onRefreshStarted(state: MainState): MainState = state.copy(
        isRefreshing = true,
    )

    fun onRefreshSucceeded(state: MainState, overview: MainOverview): MainState = state.copy(
        balances = overview.toBalancesState(isCached = false),
        isRefreshing = false,
        banner = overview.unreachableBannerOrNull(),
    )

    fun onRefreshFailed(state: MainState): MainState = state.copy(
        isRefreshing = false,
        banner = Banner.GenericError,
    )

    private fun MainOverview.toBalancesState(isCached: Boolean): BalancesState.Ready =
        BalancesState.Ready(
            totalUsd = totalUsd,
            tokens = balances,
            isStale = isCached || unreachableNetworks.isNotEmpty(),
        )

    private fun MainOverview.unreachableBannerOrNull(): Banner? =
        if (unreachableNetworks.isEmpty()) null
        else Banner.NetworksUnavailable(unreachableNetworks)

    private fun nextBannerAfterOverview(
        event: MainOverviewEvent.Data,
        previous: Banner?,
    ): Banner? {
        val unreachable = event.overview.unreachableBannerOrNull()
        return when {
            unreachable != null -> unreachable
            event.isCached -> previous      // cached: we don't know what's up yet, keep UI stable
            else -> null                    // fresh success: clear stale banner
        }
    }
}
