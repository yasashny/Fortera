package com.yasashny.fortera.feature.main.domain

/**
 * Stream of events describing what happens to the main overview data.
 *
 * A full wallet session typically produces:
 *   WalletActivated -> [cached Data] -> fresh Data | Failed
 *
 * Switching wallets cancels the previous session and starts a new one.
 */
sealed interface MainOverviewEvent {

    /** No wallets exist — caller should route to startup. */
    data object NoWallets : MainOverviewEvent

    /** A wallet became active. Emitted before its first data event. */
    data class WalletActivated(val walletId: String, val walletName: String) : MainOverviewEvent

    /** Fresh or cached overview data. */
    data class Data(val overview: MainOverview, val isCached: Boolean) : MainOverviewEvent

    /** Remote load failed and no cached data was available. */
    data class Failed(val walletId: String, val cause: Throwable) : MainOverviewEvent
}
