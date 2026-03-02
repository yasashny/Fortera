package com.yasashny.fortera.feature.managetokens

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.yasashny.fortera.core.cryptoapi.TokenCatalog
import com.yasashny.fortera.core.cryptoapi.TokenPreferences
import com.yasashny.fortera.core.cryptoapi.client.CoinGeckoClient
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.Effect
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.Intent
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.State
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.TokenItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ManageTokensViewModel(
    private val dataStore: DataStore<Preferences>,
    private val coinGeckoClient: CoinGeckoClient,
    private val ioDispatcher: CoroutineDispatcher,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            launch {
                // Шаг 1: получаем enabledIds и сразу показываем список (без цен)
                val enabledIds = dataStore.data.first()[TokenPreferences.ENABLED_TOKENS_KEY]
                    ?: TokenCatalog.defaultTokenIds
                val initialTokens = TokenCatalog.tokens.map { token ->
                    TokenItem(token = token, isEnabled = token.id in enabledIds)
                }
                reduce(State(tokens = initialTokens, isLoading = true))

                // Шаг 2: слушаем изменения toggle (обновляем только isEnabled, цены сохраняются)
                launch {
                    dataStore.data
                        .map { prefs ->
                            prefs[TokenPreferences.ENABLED_TOKENS_KEY] ?: TokenCatalog.defaultTokenIds
                        }
                        .collect { ids ->
                            reduce(currentState.copy(
                                tokens = currentState.tokens.map { it.copy(isEnabled = it.token.id in ids) },
                            ))
                        }
                }

                // Шаг 3: подгружаем цены с CoinGecko на IO (обновляем только цены, isEnabled сохраняется)
                launch {
                    val ids = TokenCatalog.tokens.map { it.coingeckoId }
                    runCatching {
                        withContext(ioDispatcher) { coinGeckoClient.getPrices(ids) }
                    }.onSuccess { prices ->
                        reduce(currentState.copy(
                            tokens = currentState.tokens.map { item ->
                                val (price, change) = prices[item.token.coingeckoId] ?: (0.0 to 0.0)
                                item.copy(priceUsd = price, changePercent24h = change)
                            },
                            isLoading = false,
                        ))
                    }.onFailure {
                        reduce(currentState.copy(isLoading = false))
                    }
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Toggle -> intent {
                launch {
                    dataStore.edit { prefs ->
                        val current = prefs[TokenPreferences.ENABLED_TOKENS_KEY]
                            ?: TokenCatalog.defaultTokenIds
                        prefs[TokenPreferences.ENABLED_TOKENS_KEY] =
                            if (intent.tokenId in current) current - intent.tokenId
                            else current + intent.tokenId
                    }
                }
            }
        }
    }
}
