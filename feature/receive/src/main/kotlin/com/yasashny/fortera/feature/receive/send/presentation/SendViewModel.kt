package com.yasashny.fortera.feature.receive.send.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.AddressValidator
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.core.walletbalances.WalletAddressesService
import com.yasashny.fortera.feature.receive.R
import kotlinx.coroutines.flow.first
import java.math.BigDecimal

internal class SendViewModel(
    private val tokenId: String,
    private val walletInteractor: WalletInteractor,
    private val balanceRepository: BalanceRepository,
    private val tokenRepository: TokenRepository,
    private val walletAddressesService: WalletAddressesService,
    private val addressValidator: AddressValidator,
) : MviViewModel<SendState, SendIntent, SendEffect>(SendState()) {

    private var token: TokenDefinition? = null

    init {
        intent {
            val loadedToken = tokenRepository.getTokenById(tokenId)
            if (loadedToken == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }
            token = loadedToken
            reduce(
                currentState.copy(
                    tokenId = tokenId,
                    tokenName = loadedToken.name,
                    tokenSymbol = loadedToken.symbol,
                    tokenIconUrl = tokenIconUrl(loadedToken),
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

    override fun handleIntent(intent: SendIntent) {
        when (intent) {
            is SendIntent.UpdateAmount -> onAmountChanged(intent.amount)
            is SendIntent.UpdateAddress -> onAddressChanged(intent.address)
            SendIntent.Continue -> onContinue()
        }
    }

    private fun onAmountChanged(raw: String) {
        val filtered = filterAmountInput(raw)
        val amountError: UiText? = if (filtered != raw)
            UiText.of(R.string.send_error_amount_only_digits) else null

        val amountDecimal = filtered.normalizeDecimal().toBigDecimalOrNull()
        val usdValue: Double? = amountDecimal?.let { it.toDouble() * currentState.priceUsd }
        val insufficient = amountDecimal != null && amountDecimal > currentState.balance
        updateState {
            it.copy(
                amount = filtered,
                amountUsd = usdValue,
                insufficientFunds = insufficient,
                amountError = amountError,
            )
        }
    }

    private fun filterAmountInput(input: String): String {
        val sb = StringBuilder(input.length)
        var separatorSeen = false
        for (c in input) {
            when {
                c.isDigit() -> sb.append(c)
                (c == '.' || c == ',') && !separatorSeen -> {
                    sb.append(c)
                    separatorSeen = true
                }
            }
        }
        return sb.toString()
    }

    private fun onAddressChanged(raw: String) = intent {
        val trimmed = raw.trim()
        val network = token?.network
        val error: UiText? = when {
            trimmed.isEmpty() -> null
            network == null -> null
            addressValidator.isValid(trimmed, network) -> null
            else -> UiText.of(invalidAddressStringFor(network))
        }
        reduce(currentState.copy(address = trimmed, addressError = error))
    }

    private fun onContinue() {
        val snapshot = currentState
        if (!snapshot.canContinue) return
        sendEffect(
            SendEffect.NavigateToConfirm(
                tokenId = snapshot.tokenId,
                amount = snapshot.amount.normalizeDecimal(),
                address = snapshot.address,
            )
        )
    }

    private fun invalidAddressStringFor(network: BlockchainNetwork): Int = when (network) {
        BlockchainNetwork.ETHEREUM -> R.string.send_error_invalid_eth_address
        BlockchainNetwork.BITCOIN -> R.string.send_error_invalid_btc_address
    }
}
