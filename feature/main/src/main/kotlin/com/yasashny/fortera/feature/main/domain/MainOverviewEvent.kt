package com.yasashny.fortera.feature.main.domain

sealed interface MainOverviewEvent {

    data object NoWallets : MainOverviewEvent

    data class WalletActivated(val walletId: String, val walletName: String) : MainOverviewEvent

    data class Data(val overview: MainOverview, val isCached: Boolean) : MainOverviewEvent

    data class Failed(val walletId: String, val cause: Throwable) : MainOverviewEvent
}
