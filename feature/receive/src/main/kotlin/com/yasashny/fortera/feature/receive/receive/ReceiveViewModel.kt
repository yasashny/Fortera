package com.yasashny.fortera.feature.receive.receive

import com.yasashny.fortera.core.domaincrypto.HdWallet
import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.receive.receive.ReceiveContract.Effect
import com.yasashny.fortera.feature.receive.receive.ReceiveContract.Intent
import com.yasashny.fortera.feature.receive.receive.ReceiveContract.State
import kotlinx.coroutines.flow.first

internal class ReceiveViewModel(
    private val tokenId: String,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            val token = TokenCatalog.tokens.find { it.id == tokenId }
            if (token == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }
            val networkName = when (token.network) {
                BlockchainNetwork.ETHEREUM -> "Ethereum (ERC-20)"
                BlockchainNetwork.BITCOIN -> "Bitcoin"
            }
            val networkIconUrl = if (token.contractAddress != null) {
                "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/eth.png"
            } else {
                null
            }
            reduce(currentState.copy(
                tokenName = token.name,
                tokenSymbol = token.symbol,
                networkName = networkName,
                networkIconUrl = networkIconUrl,
            ))

            val wallet = walletInteractor.observeActiveWallet().first()
            if (wallet == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }
            val seed = walletInteractor.getSeedPhrase(wallet.id)
                .getOrNull()?.toDisplayString()
            if (seed == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }

            val address = when (token.network) {
                BlockchainNetwork.ETHEREUM -> HdWallet.deriveEthAddress(seed)
                BlockchainNetwork.BITCOIN -> HdWallet.deriveBtcAddress(seed)
            }

            reduce(currentState.copy(address = address, isLoading = false))
        }
    }

    override fun handleIntent(intent: Intent) {}
}
