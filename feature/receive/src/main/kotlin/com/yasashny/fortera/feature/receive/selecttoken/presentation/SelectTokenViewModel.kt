package com.yasashny.fortera.feature.receive.selecttoken.presentation

import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel

internal class SelectTokenViewModel(
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
