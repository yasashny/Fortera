package com.yasashny.fortera.feature.receive.confirmsend.presentation

import androidx.lifecycle.viewModelScope
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionError
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.core.walletbalances.WalletBalances
import com.yasashny.fortera.core.walletbalances.WalletTransactionSender
import com.yasashny.fortera.feature.receive.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

internal class ConfirmSendViewModel(
    private val tokenId: String,
    amount: String,
    private val address: String,
    private val walletInteractor: WalletInteractor,
    private val priceRepository: PriceRepository,
    private val tokenRepository: TokenRepository,
    private val walletBalances: WalletBalances,
    private val transactionSender: WalletTransactionSender,
) : MviViewModel<ConfirmSendState, ConfirmSendIntent, ConfirmSendEffect>(ConfirmSendState()) {

    private var token: TokenDefinition? = null
    private val amountDecimal: BigDecimal = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    private val amountDouble: Double = amountDecimal.toDouble()
    private var tokenPriceUsd: Double = 0.0
    private var nativePriceUsd: Double = 0.0
    private var nativeBalance: BigDecimal = BigDecimal.ZERO
    private var walletId: String? = null
    private var nativeTokenId: String = ""
    private var pollingJob: Job? = null

    init {
        intent {
            val loadedToken = tokenRepository.getTokenById(tokenId)
            if (loadedToken == null) {
                reduce(
                    currentState.copy(
                        isLoading = false,
                        errorMessage = UiText.of(R.string.send_confirm_error_token_not_found),
                    )
                )
                return@intent
            }
            token = loadedToken
            nativeTokenId = TokenCatalog.nativeToken(loadedToken.network)?.id ?: loadedToken.id

            val wallet = walletInteractor.observeActiveWallet().first()
            walletId = wallet?.id

            refreshPrices(loadedToken)
            refreshNativeBalance(wallet?.id)

            reduce(
                ConfirmSendState(
                    tokenName = loadedToken.name,
                    tokenSymbol = loadedToken.symbol,
                    tokenIconUrl = tokenIconUrl(loadedToken),
                    walletName = wallet?.name?.takeIf { it.isNotBlank() }.orEmpty(),
                    amount = "${formatCrypto(amountDouble)} ${loadedToken.symbol}",
                    amountUsd = amountDouble * tokenPriceUsd,
                    address = address,
                    networkName = loadedToken.network.displayName,
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

    override fun handleIntent(intent: ConfirmSendIntent) {
        when (intent) {
            ConfirmSendIntent.Send -> sendTransaction()

            ConfirmSendIntent.OpenSpeedSheet -> updateState { it.copy(isSpeedSheetOpen = true) }
            ConfirmSendIntent.DismissSpeedSheet -> updateState { it.copy(isSpeedSheetOpen = false) }

            ConfirmSendIntent.OpenAddressSheet -> updateState { it.copy(isAddressSheetOpen = true) }
            ConfirmSendIntent.DismissAddressSheet -> updateState { it.copy(isAddressSheetOpen = false) }

            is ConfirmSendIntent.SelectSpeed -> {
                val commission = currentState.commissions[intent.speed] ?: return
                val (totalAmount, totalAmountUsd) = computeTotal(
                    tokenSymbol = currentState.tokenSymbol,
                    commission = commission,
                )
                updateState {
                    it.copy(
                        selectedSpeed = intent.speed,
                        totalAmount = totalAmount,
                        totalAmountUsd = totalAmountUsd,
                        insufficientGas = isInsufficientGas(commission),
                        isSpeedSheetOpen = false,
                    )
                }
            }

            ConfirmSendIntent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }

    private fun startFeePolling(forToken: TokenDefinition) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                runCatching {
                    refreshPrices(forToken)
                    refreshNativeBalance(walletId)
                    refreshFees(forToken)
                }
                delay(FEE_REFRESH_MS)
            }
        }
    }

    private suspend fun refreshPrices(forToken: TokenDefinition) {
        val ids = listOf(forToken.id, nativeTokenId).distinct()
        val prices = runCatching { priceRepository.getPrices(ids) }.getOrNull().orEmpty()
        tokenPriceUsd = prices[forToken.id]?.priceUsd ?: tokenPriceUsd
        nativePriceUsd = prices[nativeTokenId]?.priceUsd ?: nativePriceUsd
        if (nativePriceUsd == 0.0) nativePriceUsd = tokenPriceUsd
    }

    private suspend fun refreshNativeBalance(walletId: String?) {
        if (walletId == null) return
        val snapshot = runCatching { walletBalances.refresh(walletId) }.getOrNull()?.getOrNull()
            ?: return
        nativeBalance = snapshot.balances.firstOrNull { it.token.id == nativeTokenId }
            ?.balance ?: BigDecimal.ZERO
    }

    private suspend fun refreshFees(forToken: TokenDefinition) {
        val ownerId = walletId ?: return
        val estimates = transactionSender.estimateFees(ownerId, forToken, amountDecimal).getOrNull()

        intent {
            if (estimates == null) {
                reduce(currentState.copy(isFeesLoading = currentState.commissions.isEmpty()))
                return@intent
            }
            val commissions = toCommissions(estimates)
            val selected = currentState.selectedSpeed.takeIf { commissions.containsKey(it) }
                ?: FeeSpeed.FAST
            val commission = commissions.getValue(selected)
            val (totalAmount, totalAmountUsd) = computeTotal(
                tokenSymbol = currentState.tokenSymbol,
                commission = commission,
            )
            reduce(
                currentState.copy(
                    commissions = commissions,
                    selectedSpeed = selected,
                    totalAmount = totalAmount,
                    totalAmountUsd = totalAmountUsd,
                    insufficientGas = isInsufficientGas(commission),
                    isFeesLoading = false,
                    amountUsd = amountDouble * tokenPriceUsd,
                )
            )
        }
    }

    private fun toCommissions(estimates: FeeEstimates): Map<FeeSpeed, CommissionInfo> =
        estimates.mapValues { (_, estimate) ->
            CommissionInfo(
                estimate = estimate,
                nativeAmount = "${formatCrypto(estimate.nativeAmount.toDouble())} ${estimate.nativeSymbol}",
                feeUsd = estimate.nativeAmount.toDouble() * nativePriceUsd,
            )
        }

    private fun computeTotal(
        tokenSymbol: String,
        commission: CommissionInfo,
    ): Pair<String, Double> {
        val feeNative: BigDecimal = commission.estimate.nativeAmount
        val feeSymbol = commission.estimate.nativeSymbol
        val sameUnit = tokenSymbol.equals(feeSymbol, ignoreCase = true)

        val totalNative: BigDecimal = if (sameUnit) amountDecimal + feeNative else amountDecimal
        val totalUsd: Double =
            amountDecimal.toDouble() * tokenPriceUsd + feeNative.toDouble() * nativePriceUsd

        return "${formatCrypto(totalNative.toDouble())} $tokenSymbol" to totalUsd
    }

    private fun isInsufficientGas(commission: CommissionInfo): Boolean {
        val tokenSymbol = token?.symbol ?: return false
        val feeNative = commission.estimate.nativeAmount
        val feeSymbol = commission.estimate.nativeSymbol
        val available = if (tokenSymbol.equals(feeSymbol, ignoreCase = true)) {
            nativeBalance - amountDecimal
        } else {
            nativeBalance
        }
        return available < feeNative
    }

    private fun sendTransaction() {
        val activeToken = token ?: return
        val owner = walletId ?: return
        intent {
            if (currentState.commissions.isEmpty() || currentState.isSending || currentState.insufficientGas) return@intent
            reduce(currentState.copy(isSending = true, errorMessage = null))

            transactionSender.send(
                walletId = owner,
                token = activeToken,
                toAddress = address,
                amount = amountDecimal,
                speed = currentState.selectedSpeed,
            ).fold(
                onSuccess = {
                    reduce(currentState.copy(isSending = false))
                    sendEffect(
                        ConfirmSendEffect.NavigateToSuccess(
                            amount = formatCrypto(amountDouble),
                            symbol = activeToken.symbol,
                        )
                    )
                },
                onFailure = { throwable ->
                    reduce(currentState.copy(isSending = false, errorMessage = mapSendError(throwable)))
                },
            )
        }
    }

    private fun mapSendError(throwable: Throwable): UiText = when (throwable) {
        is SendTransactionError.NoConfirmedUtxos ->
            UiText.of(R.string.send_confirm_error_no_utxos)
        is SendTransactionError.InsufficientFunds ->
            UiText.of(R.string.send_confirm_error_insufficient_funds)
        else -> throwable.message?.takeIf { it.isNotBlank() }?.let(UiText::of)
            ?: UiText.of(R.string.send_confirm_error_send_failed)
    }

    private companion object {
        const val FEE_REFRESH_MS = 15_000L
    }
}

private fun formatCrypto(value: Double): String =
    String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
