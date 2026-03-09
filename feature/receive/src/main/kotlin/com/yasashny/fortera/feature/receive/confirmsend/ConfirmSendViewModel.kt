package com.yasashny.fortera.feature.receive.confirmsend

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.Effect
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.Intent
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.State
import kotlinx.coroutines.delay
import java.util.Locale

internal class ConfirmSendViewModel(
    private val tokenId: String,
    private val amount: String,
    private val address: String,
    private val priceRepository: PriceRepository,
    private val tokenRepository: TokenRepository,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        intent {
            val token = tokenRepository.getTokenById(tokenId)
            if (token == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }

            val networkName = when (token.network) {
                BlockchainNetwork.ETHEREUM -> "Ethereum"
                BlockchainNetwork.BITCOIN -> "Bitcoin"
            }

            val prices = priceRepository.getPricesLocalOrRemote(listOf(tokenId))
            val priceUsd = prices[tokenId]?.priceUsd ?: 0.0
            val amountDecimal = amount.toBigDecimalOrNull()?.toDouble() ?: 0.0
            val amountUsd = String.format(Locale.US, "%.2f", amountDecimal * priceUsd)

            // Estimate commission (placeholder: ~0.000150 ETH for Ethereum, ~0.00005 BTC for Bitcoin)
            val commissionAmount = when (token.network) {
                BlockchainNetwork.ETHEREUM -> "0.000150"
                BlockchainNetwork.BITCOIN -> "0.000050"
            }
            val commissionUsd = when (token.network) {
                BlockchainNetwork.ETHEREUM -> {
                    val ethPrice = prices["ethereum"]?.priceUsd ?: priceUsd
                    String.format(Locale.US, "%.2f", 0.000150 * ethPrice)
                }
                BlockchainNetwork.BITCOIN -> {
                    String.format(Locale.US, "%.2f", 0.000050 * priceUsd)
                }
            }
            val networkSymbol = when (token.network) {
                BlockchainNetwork.ETHEREUM -> "ETH"
                BlockchainNetwork.BITCOIN -> "BTC"
            }
            val commission = "$commissionAmount $networkSymbol ~ $commissionUsd $"

            reduce(
                State(
                    tokenName = token.name,
                    tokenSymbol = token.symbol,
                    amount = "$amount ${token.symbol}",
                    amountUsd = "$amountUsd $",
                    address = address,
                    networkName = networkName,
                    commission = commission,
                    isLoading = false,
                )
            )
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Send -> intent {
                reduce(currentState.copy(isSending = true))
                // Simulate send transaction
                delay(2000)
                reduce(currentState.copy(isSending = false))
                sendEffect(Effect.NavigateToSuccess)
            }
        }
    }
}
