package com.yasashny.fortera.feature.receive.receive

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.token.networkBadgeUrlFor
import com.yasashny.fortera.core.walletbalances.WalletAddressesService
import com.yasashny.fortera.feature.receive.receive.ReceiveContract.Effect
import com.yasashny.fortera.feature.receive.receive.ReceiveContract.Intent
import com.yasashny.fortera.feature.receive.receive.ReceiveContract.State

internal class ReceiveViewModel(
    private val tokenId: String,
    private val tokenRepository: TokenRepository,
    private val walletAddressesService: WalletAddressesService,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            val token = tokenRepository.getTokenById(tokenId)
            if (token == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }
            val networkName = when (token.network) {
                BlockchainNetwork.ETHEREUM -> "Ethereum (ERC-20)"
                BlockchainNetwork.BITCOIN -> "Bitcoin"
            }
            val networkIconUrl = networkBadgeUrlFor(token)
            reduce(currentState.copy(
                tokenName = token.name,
                tokenSymbol = token.symbol,
                networkName = networkName,
                networkIconUrl = networkIconUrl,
            ))

            val addresses = walletAddressesService.forActiveWallet()
            if (addresses == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }

            val address = when (token.network) {
                BlockchainNetwork.ETHEREUM -> addresses.eth
                BlockchainNetwork.BITCOIN -> addresses.btc
            }

            reduce(currentState.copy(address = address, isLoading = false))
        }
    }

    override fun handleIntent(intent: Intent) {}
}
