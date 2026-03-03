package com.yasashny.fortera.feature.managetokens

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.TokenPreferences
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.Effect
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.Intent
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.State
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.TokenItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ManageTokensViewModel(
    private val dataStore: DataStore<Preferences>,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            launch {
                val enabledIds = dataStore.data.first()[TokenPreferences.ENABLED_TOKENS_KEY]
                    ?: TokenCatalog.defaultTokenIds
                val initialTokens = TokenCatalog.tokens.map { token ->
                    TokenItem(token = token, isEnabled = token.id in enabledIds)
                }
                reduce(State(tokens = initialTokens, isLoading = false))

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
