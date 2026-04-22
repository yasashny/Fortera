package com.yasashny.fortera.core.walletbalances

/**
 * Emitted by [WalletBalances.observe]. A typical wallet session looks like:
 *
 *   [Loading] -> (optional) [Snapshot]cached -> [Snapshot]fresh
 *
 * or on complete failure with no cache:
 *
 *   [Loading] -> [Failed]
 */
sealed interface WalletBalancesEvent {

    /** New session has begun — clear any previous data on the UI. */
    data class Loading(val walletId: String) : WalletBalancesEvent

    /** Balances available (cached or fresh). */
    data class Snapshot(val value: WalletBalancesSnapshot) : WalletBalancesEvent

    /** Fetch failed and no cached data was available. */
    data class Failed(val walletId: String, val cause: Throwable) : WalletBalancesEvent
}
