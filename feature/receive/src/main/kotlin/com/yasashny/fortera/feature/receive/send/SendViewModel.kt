package com.yasashny.fortera.feature.receive.send

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.walletbalances.WalletAddressesService
import com.yasashny.fortera.feature.receive.send.SendContract.Effect
import com.yasashny.fortera.feature.receive.send.SendContract.Intent
import com.yasashny.fortera.feature.receive.send.SendContract.State
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.Locale

internal class SendViewModel(
    private val tokenId: String,
    private val walletInteractor: WalletInteractor,
    private val balanceRepository: BalanceRepository,
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
            reduce(
                currentState.copy(
                    tokenId = tokenId,
                    tokenName = token.name,
                    tokenSymbol = token.symbol,
                )
            )

            val wallet = walletInteractor.observeActiveWallet().first()
            if (wallet == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }
            val addresses = walletAddressesService.forWallet(wallet.id)
            if (addresses == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }

            balanceRepository.getTokenBalances(
                walletId = wallet.id,
                ethAddress = addresses.eth,
                btcAddress = addresses.btc,
                enabledTokenIds = setOf(tokenId),
            ).onSuccess { result ->
                val tokenBalance = result.balances.firstOrNull()
                reduce(
                    currentState.copy(
                        balance = tokenBalance?.balance ?: BigDecimal.ZERO,
                        priceUsd = tokenBalance?.priceUsd ?: 0.0,
                        isLoading = false,
                    )
                )
            }.onFailure {
                reduce(currentState.copy(isLoading = false))
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateAmount -> intent {
                val amount = intent.amount
                val amountDecimal = amount.toBigDecimalOrNull()
                val usdValue = if (amountDecimal != null) {
                    String.format(Locale.US, "%.2f", amountDecimal.toDouble() * currentState.priceUsd)
                } else {
                    ""
                }
                val insufficient = amountDecimal != null && amountDecimal > currentState.balance
                reduce(
                    currentState.copy(
                        amount = amount,
                        amountUsd = usdValue,
                        insufficientFunds = insufficient,
                    )
                )
            }

            is Intent.UpdateAddress -> intent {
                reduce(currentState.copy(address = intent.address))
            }

            Intent.Continue -> intent {
                val amount = currentState.amount.toBigDecimalOrNull()
                if (amount == null || amount <= BigDecimal.ZERO) return@intent
                if (currentState.address.isBlank()) return@intent
                if (currentState.insufficientFunds) return@intent
                sendEffect(
                    Effect.NavigateToConfirm(
                        tokenId = currentState.tokenId,
                        amount = currentState.amount,
                        address = currentState.address,
                    )
                )
            }
        }
    }
}
