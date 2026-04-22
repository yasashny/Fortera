package com.yasashny.fortera.feature.main.domain

import com.yasashny.fortera.core.common.Interactor
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.walletbalances.WalletBalances
import com.yasashny.fortera.core.walletbalances.WalletBalancesEvent
import com.yasashny.fortera.core.walletbalances.WalletBalancesSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

/**
 * Projects [WalletBalances] events plus active-wallet tracking into the feature-level
 * [MainOverviewEvent] stream. No seed / address / network code here — that all lives in
 * [WalletBalances] now.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainOverviewInteractor(
    private val walletInteractor: WalletInteractor,
    private val walletBalances: WalletBalances,
    dispatcher: CoroutineDispatcher,
) : Interactor(dispatcher) {

    fun observe(): Flow<MainOverviewEvent> =
        walletInteractor.getWallets()
            .combine(walletInteractor.observeActiveWallet()) { wallets, active ->
                Activation(hasWallets = wallets.isNotEmpty(), wallet = active)
            }
            .distinctUntilChanged { old, new ->
                old.hasWallets == new.hasWallets && old.wallet?.id == new.wallet?.id
            }
            .flatMapLatest { activation ->
                when {
                    !activation.hasWallets -> flowOf(MainOverviewEvent.NoWallets)
                    activation.wallet == null -> emptyFlow()
                    else -> walletSession(activation.wallet)
                }
            }
            .flowOn(dispatcher)

    suspend fun refresh(): Result<MainOverview> = execute {
        val wallet = walletInteractor.observeActiveWallet().first()
            ?: error("No active wallet")
        val snapshot = walletBalances.refresh(wallet.id).getOrThrow()
        snapshot.toOverview()
    }

    private fun walletSession(wallet: Wallet): Flow<MainOverviewEvent> = flow {
        emit(MainOverviewEvent.WalletActivated(wallet.id, wallet.name))
        walletBalances.observe(wallet.id).collect { event ->
            when (event) {
                is WalletBalancesEvent.Loading -> Unit // WalletActivated already signalled the reset
                is WalletBalancesEvent.Snapshot -> emit(
                    MainOverviewEvent.Data(
                        overview = event.value.toOverview(),
                        isCached = event.value.isFromCache,
                    )
                )
                is WalletBalancesEvent.Failed -> emit(MainOverviewEvent.Failed(wallet.id, event.cause))
            }
        }
    }

    private fun WalletBalancesSnapshot.toOverview(): MainOverview = MainOverview(
        walletId = walletId,
        balances = balances,
        totalUsd = totalUsd,
        unreachableNetworks = unreachableNetworks,
    )

    private data class Activation(val hasWallets: Boolean, val wallet: Wallet?)
}
