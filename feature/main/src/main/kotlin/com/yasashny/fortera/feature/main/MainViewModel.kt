package com.yasashny.fortera.feature.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.yasashny.fortera.core.cryptoapi.CryptoRepository
import com.yasashny.fortera.core.cryptoapi.HdWallet
import com.yasashny.fortera.core.cryptoapi.TokenCatalog
import com.yasashny.fortera.core.cryptoapi.TokenPreferences
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.main.MainContract.Effect
import com.yasashny.fortera.feature.main.MainContract.Intent
import com.yasashny.fortera.feature.main.MainContract.State
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class MainViewModel(
    private val walletInteractor: WalletInteractor,
    private val cryptoRepository: CryptoRepository,
    private val dataStore: DataStore<Preferences>,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            launch {
                combine(
                    walletInteractor.getWallets(),
                    walletInteractor.observeActiveWallet(),
                    dataStore.data,
                ) { wallets, activeWallet, prefs ->
                    Triple(wallets, activeWallet, prefs)
                }.collect { (wallets, activeWallet, prefs) ->
                    if (wallets.isEmpty()) {
                        sendEffect(Effect.NavigateToStartup)
                        return@collect
                    }
                    val wallet = activeWallet ?: return@collect
                    reduce(currentState.copy(activeWalletName = wallet.name, isLoading = true))

                    val seed = walletInteractor.getSeedPhrase(wallet.id)
                        .getOrNull()?.toDisplayString() ?: return@collect

                    val ethAddress = HdWallet.deriveEthAddress(seed)
                    val btcAddress = HdWallet.deriveBtcAddress(seed)
                    val enabledIds = prefs[TokenPreferences.ENABLED_TOKENS_KEY]
                        ?: TokenCatalog.defaultTokenIds

                    cryptoRepository.getTokenBalances(ethAddress, btcAddress, enabledIds)
                        .onSuccess { tokens ->
                            reduce(State(
                                activeWalletName = wallet.name,
                                totalUsd = tokens.sumOf { it.balanceUsd },
                                tokens = tokens,
                                isLoading = false,
                            ))
                        }
                        .onFailure {
                            reduce(currentState.copy(isLoading = false))
                        }
                }
            }

            launch {
                while (true) {
                    delay(30_000)
                    val wallet = walletInteractor.observeActiveWallet().first() ?: continue
                    val prefs = dataStore.data.first()
                    val seed = walletInteractor.getSeedPhrase(wallet.id)
                        .getOrNull()?.toDisplayString() ?: continue

                    val ethAddress = HdWallet.deriveEthAddress(seed)
                    val btcAddress = HdWallet.deriveBtcAddress(seed)
                    val enabledIds = prefs[TokenPreferences.ENABLED_TOKENS_KEY]
                        ?: TokenCatalog.defaultTokenIds

                    cryptoRepository.getTokenBalances(ethAddress, btcAddress, enabledIds)
                        .onSuccess { tokens ->
                            reduce(State(
                                activeWalletName = wallet.name,
                                totalUsd = tokens.sumOf { it.balanceUsd },
                                tokens = tokens,
                                isLoading = false,
                            ))
                        }
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OpenManageTokens -> intent { sendEffect(Effect.NavigateToManageTokens) }
            Intent.OpenSettings -> intent { sendEffect(Effect.NavigateToSettings) }
            Intent.OpenWalletSelector -> Unit
        }
    }
}
