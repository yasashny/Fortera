package com.yasashny.fortera.feature.managetokens

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.Effect
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.Intent
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.State
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.TokenItem
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ManageTokensViewModel(
    private val tokenRepository: TokenRepository,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var activeWalletId: String? = null

    init {
        intent {
            launch {
                val wallet = walletInteractor.observeActiveWallet().first() ?: return@launch
                activeWalletId = wallet.id

                combine(
                    tokenRepository.observeAllTokens(),
                    tokenRepository.observeEnabledTokenIds(wallet.id),
                ) { tokens, enabledIds ->
                    tokens.map { TokenItem(token = it, isEnabled = it.id in enabledIds) }
                }.collect { items ->
                    reduce(State(tokens = items, isLoading = false))
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Toggle -> intent {
                val walletId = activeWalletId ?: return@intent
                launch {
                    val isCurrentlyEnabled = currentState.tokens
                        .find { it.token.id == intent.tokenId }?.isEnabled ?: return@launch
                    tokenRepository.setTokenEnabled(walletId, intent.tokenId, !isCurrentlyEnabled)
                }
            }
        }
    }
}
