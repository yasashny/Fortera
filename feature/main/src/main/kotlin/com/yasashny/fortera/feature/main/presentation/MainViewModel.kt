package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.main.presentation.MainContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val walletInteractor: WalletInteractor,
    private val balanceRepository: BalanceRepository,
    private val tokenRepository: TokenRepository,
    private val addressResolver: AddressResolver,
) : MviViewModel<MainContract.State, MainContract.Intent, MainContract.Effect>(MainContract.State()) {

    init {
        intent {
            launch {
                walletInteractor.observeActiveWallet()
                    .flatMapLatest { activeWallet ->
                        if (activeWallet == null) return@flatMapLatest flowOf(null)
                        tokenRepository.getEnabledTokenIds(activeWallet.id)
                        tokenRepository.observeEnabledTokenIds(activeWallet.id)
                            .map { enabledIds -> activeWallet to enabledIds }
                    }
                    .combine(walletInteractor.getWallets()) { walletWithTokens, wallets ->
                        Triple(wallets, walletWithTokens?.first, walletWithTokens?.second)
                    }
                    .collect { (wallets, activeWallet, enabledIds) ->
                        if (wallets.isEmpty()) {
                            sendEffect(MainContract.Effect.NavigateToStartup)
                            return@collect
                        }
                        val wallet = activeWallet ?: return@collect
                        val ids = enabledIds ?: return@collect

                        val isFirstLoad = currentState.isLoading

                        if (isFirstLoad) {
                            val cached = balanceRepository.getCachedBalances(wallet.id, ids)
                            if (cached != null) {
                                reduce(
                                    MainContract.State(
                                        activeWalletName = wallet.name,
                                        totalUsd = cached.sumOf { it.balanceUsd },
                                        tokens = cached,
                                        isLoading = false,
                                        isCached = true,
                                    )
                                )
                            } else {
                                reduce(currentState.copy(activeWalletName = wallet.name, isLoading = true))
                            }
                        } else {
                            reduce(currentState.copy(activeWalletName = wallet.name, isCached = true))
                        }

                        val seed = walletInteractor.getSeedPhrase(wallet.id)
                            .getOrNull()?.toDisplayString() ?: return@collect

                        val ethAddress = addressResolver.ethAddress(seed)
                        val btcAddress = addressResolver.btcAddress(seed)

                        balanceRepository.getTokenBalances(wallet.id, ethAddress, btcAddress, ids)
                            .onSuccess { result ->
                                balanceRepository.cacheBalances(wallet.id, result.balances)
                                reduce(
                                    MainContract.State(
                                        activeWalletName = wallet.name,
                                        totalUsd = result.balances.sumOf { it.balanceUsd },
                                        tokens = result.balances,
                                        isLoading = false,
                                        isCached = result.failedNetworks.isNotEmpty(),
                                    )
                                )
                                if (result.failedNetworks.isNotEmpty()) {
                                    sendEffect(MainContract.Effect.ShowNetworkError(result.failedNetworks))
                                }
                            }
                            .onFailure {
                                reduce(currentState.copy(isLoading = false))
                                sendEffect(MainContract.Effect.ShowError)
                            }
                    }
            }
        }
    }

    override fun handleIntent(intent: MainContract.Intent) {
        when (intent) {
            MainContract.Intent.Refresh -> intent {
                reduce(currentState.copy(isRefreshing = true))
                val wallet = walletInteractor.observeActiveWallet().first() ?: run {
                    reduce(currentState.copy(isRefreshing = false))
                    return@intent
                }
                val seed = walletInteractor.getSeedPhrase(wallet.id)
                    .getOrNull()?.toDisplayString() ?: run {
                    reduce(currentState.copy(isRefreshing = false))
                    return@intent
                }
                val ethAddress = addressResolver.ethAddress(seed)
                val btcAddress = addressResolver.btcAddress(seed)
                val enabledIds = tokenRepository.getEnabledTokenIds(wallet.id)

                balanceRepository.getTokenBalances(
                    wallet.id,
                    ethAddress,
                    btcAddress,
                    enabledIds,
                    forceRemote = true
                )
                    .onSuccess { result ->
                        balanceRepository.cacheBalances(wallet.id, result.balances)
                        reduce(
                            MainContract.State(
                                activeWalletName = wallet.name,
                                totalUsd = result.balances.sumOf { it.balanceUsd },
                                tokens = result.balances,
                                isLoading = false,
                                isRefreshing = false,
                                isCached = result.failedNetworks.isNotEmpty(),
                            )
                        )
                        if (result.failedNetworks.isNotEmpty()) {
                            sendEffect(MainContract.Effect.ShowNetworkError(result.failedNetworks))
                        }
                    }
                    .onFailure {
                        reduce(currentState.copy(isRefreshing = false))
                        sendEffect(MainContract.Effect.ShowError)
                    }
            }

            MainContract.Intent.OpenManageTokens -> intent { sendEffect(MainContract.Effect.NavigateToManageTokens) }
            MainContract.Intent.OpenSettings -> intent { sendEffect(MainContract.Effect.NavigateToSettings) }
            is MainContract.Intent.OpenTokenDetails -> intent { sendEffect(MainContract.Effect.NavigateToTokenDetails(intent.tokenId)) }
            MainContract.Intent.OpenReceive -> intent { sendEffect(MainContract.Effect.NavigateToSelectTokenForReceive) }
            MainContract.Intent.OpenSend -> intent { sendEffect(MainContract.Effect.NavigateToSelectTokenForSend) }
            MainContract.Intent.OpenWalletSelector -> Unit
        }
    }
}