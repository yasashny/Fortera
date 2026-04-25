package com.yasashny.fortera.core.walletbalances

sealed interface WalletBalancesEvent {

    data class Loading(val walletId: String) : WalletBalancesEvent

    data class Snapshot(val value: WalletBalancesSnapshot) : WalletBalancesEvent

    data class Failed(val walletId: String, val cause: Throwable) : WalletBalancesEvent
}
