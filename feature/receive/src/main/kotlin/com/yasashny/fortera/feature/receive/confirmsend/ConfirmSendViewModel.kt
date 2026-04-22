package com.yasashny.fortera.feature.receive.confirmsend

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import androidx.lifecycle.viewModelScope
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.format.formatUsd as sharedFormatUsd
import com.yasashny.fortera.core.walletbalances.WalletTransactionSender
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.CommissionInfo
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.Effect
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.Intent
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendContract.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

internal class ConfirmSendViewModel(
    private val tokenId: String,
    private val amount: String,
    private val address: String,
    private val walletInteractor: WalletInteractor,
    private val priceRepository: PriceRepository,
    private val tokenRepository: TokenRepository,
    private val transactionSender: WalletTransactionSender,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var token: TokenDefinition? = null
    private var amountDouble: Double = 0.0
    private var tokenPriceUsd: Double = 0.0
    private var nativePriceUsd: Double = 0.0
    private var pollingJob: Job? = null

    init {
        intent {
            val loadedToken = tokenRepository.getTokenById(tokenId)
            if (loadedToken == null) {
                reduce(currentState.copy(isLoading = false, errorMessage = "Token not found"))
                return@intent
            }
            token = loadedToken

            val networkName = when (loadedToken.network) {
                BlockchainNetwork.ETHEREUM -> "Ethereum"
                BlockchainNetwork.BITCOIN -> "Bitcoin"
            }
            val nativeTokenId = when (loadedToken.network) {
                BlockchainNetwork.ETHEREUM -> "ethereum"
                BlockchainNetwork.BITCOIN -> "bitcoin"
            }

            val prices = priceRepository.getPrices(
                listOf(tokenId, nativeTokenId).distinct(),
            )
            tokenPriceUsd = prices[tokenId]?.priceUsd ?: 0.0
            nativePriceUsd = prices[nativeTokenId]?.priceUsd ?: tokenPriceUsd
            amountDouble = amount.toDoubleOrNull() ?: 0.0

            val wallet = walletInteractor.observeActiveWallet().first()
            val walletName = wallet?.name?.takeIf { it.isNotBlank() } ?: "Wallet"

            reduce(
                State(
                    tokenName = loadedToken.name,
                    tokenSymbol = loadedToken.symbol,
                    walletName = walletName,
                    amount = "${formatCrypto(amountDouble)} ${loadedToken.symbol}",
                    amountUsd = formatUsd(amountDouble * tokenPriceUsd),
                    address = address,
                    networkName = networkName,
                    isLoading = false,
                    isFeesLoading = true,
                )
            )

            startFeePolling(loadedToken)
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Send -> sendTransaction()

            Intent.OpenSpeedSheet -> intent {
                reduce(currentState.copy(isSpeedSheetOpen = true))
            }

            Intent.DismissSpeedSheet -> intent {
                reduce(currentState.copy(isSpeedSheetOpen = false))
            }

            is Intent.SelectSpeed -> intent {
                val commission = currentState.commissions[intent.speed] ?: return@intent
                val (totalAmount, totalAmountUsd) = computeTotal(
                    tokenSymbol = currentState.tokenSymbol,
                    commission = commission,
                )
                reduce(
                    currentState.copy(
                        selectedSpeed = intent.speed,
                        totalAmount = totalAmount,
                        totalAmountUsd = totalAmountUsd,
                        isSpeedSheetOpen = false,
                    )
                )
            }

            Intent.DismissError -> intent {
                reduce(currentState.copy(errorMessage = null))
            }
        }
    }

    private fun startFeePolling(forToken: TokenDefinition) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refreshFees(forToken)
                delay(FEE_REFRESH_MS)
            }
        }
    }

    private suspend fun refreshFees(forToken: TokenDefinition) {
        val result = transactionSender.estimateFees(forToken)
        val estimates = result.getOrNull()
        intent {
            if (estimates == null) {
                reduce(
                    currentState.copy(
                        isFeesLoading = currentState.commissions.isEmpty(),
                    )
                )
                return@intent
            }
            val commissions = toCommissions(estimates)
            val selected = currentState.selectedSpeed.takeIf { commissions.containsKey(it) }
                ?: FeeSpeed.FAST
            val (totalAmount, totalAmountUsd) = computeTotal(
                tokenSymbol = currentState.tokenSymbol,
                commission = commissions.getValue(selected),
            )
            reduce(
                currentState.copy(
                    commissions = commissions,
                    selectedSpeed = selected,
                    totalAmount = totalAmount,
                    totalAmountUsd = totalAmountUsd,
                    isFeesLoading = false,
                )
            )
        }
    }

    private fun toCommissions(estimates: FeeEstimates): Map<FeeSpeed, CommissionInfo> =
        estimates.mapValues { (_, estimate) ->
            val nativeAmountDouble = estimate.nativeAmount.toDouble()
            val feeUsd = nativeAmountDouble * nativePriceUsd
            CommissionInfo(
                nativeAmount = "${formatCrypto(nativeAmountDouble)} ${estimate.nativeSymbol}",
                usdAmount = formatUsd(feeUsd),
            )
        }

    private fun sendTransaction() {
        val activeToken = token ?: return
        intent {
            if (currentState.commissions.isEmpty() || currentState.isSending) return@intent
            reduce(currentState.copy(isSending = true, errorMessage = null))

            val wallet = walletInteractor.observeActiveWallet().first()
            if (wallet == null) {
                reduce(currentState.copy(isSending = false, errorMessage = "No active wallet"))
                return@intent
            }

            val result = transactionSender.send(
                walletId = wallet.id,
                token = activeToken,
                toAddress = address,
                amount = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                speed = currentState.selectedSpeed,
            )
            result.fold(
                onSuccess = {
                    reduce(currentState.copy(isSending = false))
                    sendEffect(Effect.NavigateToSuccess)
                },
                onFailure = { throwable ->
                    reduce(
                        currentState.copy(
                            isSending = false,
                            errorMessage = throwable.message ?: "Send failed",
                        )
                    )
                },
            )
        }
    }

    private fun computeTotal(tokenSymbol: String, commission: CommissionInfo): Pair<String, String> {
        val feeNative = commission.nativeAmount
            .substringBefore(' ')
            .toDoubleOrNull()
            ?: 0.0
        val feeSymbol = commission.nativeAmount.substringAfter(' ', "")
        val sameUnit = tokenSymbol.equals(feeSymbol, ignoreCase = true)
        val totalNative = if (sameUnit) amountDouble + feeNative else amountDouble
        val totalUsd = amountDouble * tokenPriceUsd + feeNative * nativePriceUsd
        return "${formatCrypto(totalNative)} $tokenSymbol" to formatUsd(totalUsd)
    }

    private companion object {
        const val FEE_REFRESH_MS = 15_000L
    }
}

/**
 * Trimmed crypto amount with no trailing zeros — the review screen prefers
 * `0.05` over `0.050000`, different from [sharedFormatCrypto]'s fixed-precision.
 */
private fun formatCrypto(value: Double): String =
    String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')

/** Fees and totals are approximate — prefix with "≈ " over the shared US-grouped format. */
private fun formatUsd(value: Double): String = "≈ ${sharedFormatUsd(value)}"
