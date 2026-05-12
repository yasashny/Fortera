package com.yasashny.fortera.feature.receive.selecttokenforsend.presentation

import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenEffect
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenIntent
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenState

internal class SelectTokenForSendViewModel(
    private val tokenRepository: TokenRepository,
) : MviViewModel<SelectTokenState, SelectTokenIntent, SelectTokenEffect>(SelectTokenState()) {

    init {
        intent {
            reduce(SelectTokenState(tokens = tokenRepository.getAllTokens(), isLoading = false))
        }
    }

    override fun handleIntent(intent: SelectTokenIntent) {
        when (intent) {
            is SelectTokenIntent.SelectToken ->
                sendEffect(SelectTokenEffect.NavigateToReceive(intent.tokenId))
        }
    }
}
