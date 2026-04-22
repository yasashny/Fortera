package com.yasashny.fortera.feature.receive.send

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.AddressValidator
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.core.walletbalances.WalletAddressesService
import com.yasashny.fortera.feature.receive.R
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
    private val addressValidator: AddressValidator,
) : MviViewModel<State, Intent, Effect>(State()) {

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
            is Intent.UpdateAmount -> onAmountChanged(intent.amount)
            is Intent.UpdateAddress -> onAddressChanged(intent.address)
            Intent.Continue -> onContinue()
        }
    }

    /**
     * Accepts only digits and at most one decimal separator. Anything else (letters, spaces,
     * symbols, a second `.`/`,`) is silently filtered out and a localised hint is surfaced
     * so the user understands why their key press didn't register.
     */
    private fun onAmountChanged(raw: String) = intent {
        val filtered = filterAmountInput(raw)
        val amountError: UiText? = if (filtered != raw)
            UiText.of(R.string.send_error_amount_only_digits) else null

        val amountDecimal = filtered.normalizeDecimal().toBigDecimalOrNull()
        val usdValue = amountDecimal
            ?.let { String.format(Locale.US, "%.2f", it.toDouble() * currentState.priceUsd) }
            ?: ""
        val insufficient = amountDecimal != null && amountDecimal > currentState.balance
        reduce(
            currentState.copy(
                amount = filtered,
                amountUsd = usdValue,
                insufficientFunds = insufficient,
                amountError = amountError,
            )
        )
    }

    /** Keep digits and at most one decimal separator; drop letters / symbols / duplicate separators. */
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
                // letters, spaces, repeated separators — dropped
            }
        }
        return sb.toString()
    }

    private fun onAddressChanged(raw: String) = intent {
        val trimmed = raw.trim()
        val network = token?.network
        val error: UiText? = when {
            trimmed.isEmpty() -> null                              // don't shame an empty field
            network == null -> null                                // token hasn't loaded yet
            addressValidator.isValid(trimmed, network) -> null
            else -> UiText.of(invalidAddressStringFor(network))
        }
        reduce(currentState.copy(address = trimmed, addressError = error))
    }

    private fun onContinue() = intent {
        val snapshot = currentState
        if (!snapshot.canContinue) return@intent
        sendEffect(
            Effect.NavigateToConfirm(
                tokenId = snapshot.tokenId,
                // Confirm screen parses with `.` — normalise before navigation.
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
