package com.yasashny.fortera.feature.receive.selecttokenforsend

import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenContract.Effect
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenContract.Intent
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenContract.State

internal class SelectTokenForSendViewModel : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            reduce(State(tokens = TokenCatalog.tokens, isLoading = false))
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.SelectToken -> intent {
                sendEffect(Effect.NavigateToReceive(intent.tokenId))
            }
        }
    }
}
