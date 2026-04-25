package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.common.Interactor
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.FetchPolicy
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface WalletBalances {
    fun observe(walletId: String): Flow<WalletBalancesEvent>

    suspend fun refresh(walletId: String): Result<WalletBalancesSnapshot>
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class WalletBalancesImpl(
    private val walletInteractor: WalletInteractor,
    private val tokenRepository: TokenRepository,
    private val balanceRepository: BalanceRepository,
    private val addresses: WalletAddressesService,
    dispatcher: CoroutineDispatcher,
) : Interactor(dispatcher), WalletBalances {

    override fun observe(walletId: String): Flow<WalletBalancesEvent> =
        tokenRepository.observeEnabledTokenIds(walletId)
            .distinctUntilChanged()
            .flatMapLatest { ids -> session(walletId, ids) }
            .flowOn(dispatcher)

    override suspend fun refresh(walletId: String): Result<WalletBalancesSnapshot> = execute {
        val ids = tokenRepository.getEnabledTokenIds(walletId)
        fetchFresh(walletId, ids, FetchPolicy.RemoteOnly)
    }

    private fun session(walletId: String, ids: Set<String>): Flow<WalletBalancesEvent> = flow {
        val cached = balanceRepository.getCachedBalances(walletId, ids)
        if (cached != null) {
            emit(
                WalletBalancesEvent.Snapshot(
                    snapshotOf(walletId, cached, emptySet(), isFromCache = true)
                )
            )
        } else {
            emit(WalletBalancesEvent.Loading(walletId))
        }

        runCatching { fetchFresh(walletId, ids, FetchPolicy.CacheFirst) }
            .onSuccess { emit(WalletBalancesEvent.Snapshot(it)) }
            .onFailure { cause ->
                if (cached == null) emit(WalletBalancesEvent.Failed(walletId, cause))
            }
    }

    private suspend fun fetchFresh(
        walletId: String,
        ids: Set<String>,
        policy: FetchPolicy,
    ): WalletBalancesSnapshot {
        val walletAddresses = addresses.forWallet(walletId)
            ?: error("Addresses unavailable for wallet $walletId")

        val result = balanceRepository
            .getTokenBalances(walletId, walletAddresses.eth, walletAddresses.btc, ids, policy)
            .getOrThrow()

        return snapshotOf(walletId, result.balances, result.failedNetworks, isFromCache = false)
    }

    private fun snapshotOf(
        walletId: String,
        balances: List<com.yasashny.fortera.core.domaincrypto.model.TokenBalance>,
        unreachable: Set<String>,
        isFromCache: Boolean,
    ): WalletBalancesSnapshot = WalletBalancesSnapshot(
        walletId = walletId,
        balances = balances,
        totalUsd = balances.sumOf { it.balanceUsd },
        unreachableNetworks = unreachable,
        isFromCache = isFromCache,
    )
}
