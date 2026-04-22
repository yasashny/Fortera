package com.yasashny.fortera.feature.main.domain

import com.yasashny.fortera.core.common.Interactor
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
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
 * Orchestrates the main-screen data pipeline: active wallet, enabled tokens,
 * cached balances, fresh balances, and failures — all as a single event stream.
 *
 * Presentation code subscribes to [observe] and projects events into UI state.
 * [refresh] triggers a forced remote reload for the currently active wallet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainOverviewInteractor(
    private val walletInteractor: WalletInteractor,
    private val balanceRepository: BalanceRepository,
    private val tokenRepository: TokenRepository,
    private val addressResolver: AddressResolver,
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
        val ids = tokenRepository.getEnabledTokenIds(wallet.id)
        fetchFresh(wallet, ids, forceRemote = true)
    }

    private fun walletSession(wallet: Wallet): Flow<MainOverviewEvent> = flow {
        emit(MainOverviewEvent.WalletActivated(wallet.id, wallet.name))
        tokenRepository.observeEnabledTokenIds(wallet.id)
            .distinctUntilChanged()
            .collect { ids ->
                val cached = balanceRepository.getCachedBalances(wallet.id, ids)
                if (cached != null) {
                    emit(
                        MainOverviewEvent.Data(
                            overview = MainOverview.of(wallet.id, cached, emptySet()),
                            isCached = true,
                        )
                    )
                }
                runCatching { fetchFresh(wallet, ids) }
                    .onSuccess { overview ->
                        emit(MainOverviewEvent.Data(overview = overview, isCached = false))
                    }
                    .onFailure { cause ->
                        if (cached == null) {
                            emit(MainOverviewEvent.Failed(wallet.id, cause))
                        }
                    }
            }
    }

    private suspend fun fetchFresh(
        wallet: Wallet,
        tokenIds: Set<String>,
        forceRemote: Boolean = false,
    ): MainOverview {
        val seed = walletInteractor.getSeedPhrase(wallet.id).getOrNull()?.toDisplayString()
            ?: error("Seed phrase unavailable for wallet ${wallet.id}")
        val ethAddress = addressResolver.ethAddress(seed)
        val btcAddress = addressResolver.btcAddress(seed)

        val result = balanceRepository
            .getTokenBalances(wallet.id, ethAddress, btcAddress, tokenIds, forceRemote)
            .getOrThrow()
        balanceRepository.cacheBalances(wallet.id, result.balances)

        return MainOverview.of(wallet.id, result.balances, result.failedNetworks)
    }

    private data class Activation(val hasWallets: Boolean, val wallet: Wallet?)
}
