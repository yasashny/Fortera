package com.yasashny.fortera.feature.receive.confirmsend

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
import com.yasashny.fortera.core.ui.format.formatUsd as sharedFormatUsd
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.core.walletbalances.WalletBalances
import com.yasashny.fortera.core.walletbalances.WalletTransactionSender
import com.yasashny.fortera.feature.receive.R
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

/**
 * Drives the "confirm send" screen.
 *
 * Every 15 seconds, three things are refreshed in parallel:
 *   1. Fee estimates from the chain-specific sender (ETH/BTC, with real amount + sender address
 *      so the fee matches what we'd actually pay),
 *   2. Spot prices for the token + the native fee currency (keeps USD display fresh while user
 *      is on screen),
 *   3. Native-balance check — if the computed fee exceeds the user's native balance (e.g. ERC-20
 *      send with insufficient ETH for gas), we mark state.insufficientGas and the Layout blocks
 *      the Send button.
 *
 * At actual send time the underlying sender re-fetches fees again, so the signed transaction is
 * always priced on latest chain state — display values are informational only.
 */
internal class ConfirmSendViewModel(
    private val tokenId: String,
    amount: String,
    private val address: String,
    private val walletInteractor: WalletInteractor,
    private val priceRepository: PriceRepository,
    private val tokenRepository: TokenRepository,
    private val walletBalances: WalletBalances,
    private val transactionSender: WalletTransactionSender,
) : MviViewModel<State, Intent, Effect>(State()) {

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
                State(
                    tokenName = loadedToken.name,
                    tokenSymbol = loadedToken.symbol,
                    walletName = wallet?.name?.takeIf { it.isNotBlank() }.orEmpty(),
                    amount = "${formatCrypto(amountDouble)} ${loadedToken.symbol}",
                    amountUsd = formatUsd(amountDouble * tokenPriceUsd),
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

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Send -> sendTransaction()

            Intent.OpenSpeedSheet -> updateState { it.copy(isSpeedSheetOpen = true) }
            Intent.DismissSpeedSheet -> updateState { it.copy(isSpeedSheetOpen = false) }

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
                        insufficientGas = isInsufficientGas(commission),
                        isSpeedSheetOpen = false,
                    )
                )
            }

            Intent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }

    // ─────────────────── Polling ───────────────────

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
                )
            )
        }
    }

    private fun toCommissions(estimates: FeeEstimates): Map<FeeSpeed, CommissionInfo> =
        estimates.mapValues { (_, estimate) ->
            val feeUsd = estimate.nativeAmount.toDouble() * nativePriceUsd
            CommissionInfo(
                estimate = estimate,
                nativeAmount = "${formatCrypto(estimate.nativeAmount.toDouble())} ${estimate.nativeSymbol}",
                usdAmount = formatUsd(feeUsd),
            )
        }

    // ─────────────────── Math ───────────────────

    /**
     * Returns (totalNativeDisplay, totalUsdDisplay). Works in BigDecimal throughout — no string parsing.
     *
     * If the token being sent IS the fee currency (native ETH / BTC), total = amount + fee.
     * Otherwise (ERC-20 vs ETH fee), the token total is just the amount and fee shows up in
     * USD addition only.
     */
    private fun computeTotal(
        tokenSymbol: String,
        commission: CommissionInfo,
    ): Pair<String, String> {
        val feeNative: BigDecimal = commission.estimate.nativeAmount
        val feeSymbol = commission.estimate.nativeSymbol
        val sameUnit = tokenSymbol.equals(feeSymbol, ignoreCase = true)

        val totalNative: BigDecimal = if (sameUnit) amountDecimal + feeNative else amountDecimal
        val totalUsd: Double =
            amountDecimal.toDouble() * tokenPriceUsd + feeNative.toDouble() * nativePriceUsd

        return "${formatCrypto(totalNative.toDouble())} $tokenSymbol" to formatUsd(totalUsd)
    }

    /**
     * True when the wallet can't cover the fee. For ERC-20 sends, compares fee (in native ETH)
     * to native balance. For native sends, subtracts amount first — there must be enough native
     * left to pay the fee after the transfer.
     */
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

    // ─────────────────── Send ───────────────────

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
                    sendEffect(Effect.NavigateToSuccess)
                },
                onFailure = { throwable ->
                    reduce(currentState.copy(isSending = false, errorMessage = mapSendError(throwable)))
                },
            )
        }
    }

    /**
     * Maps send-time exceptions to localised UI messages.
     * Known [SendTransactionError] cases resolve to feature strings; everything else falls
     * back to the exception message (usually from RPC / http layer) wrapped as a literal, or
     * the generic "send failed" resource if the message is empty.
     */
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

/**
 * Trimmed crypto amount with no trailing zeros — the review screen prefers
 * `0.05` over `0.050000`, different from the shared fixed-precision formatter.
 */
private fun formatCrypto(value: Double): String =
    String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')

/** Fees and totals are approximate — prefix with "≈ " over the shared US-grouped format. */
private fun formatUsd(value: Double): String = "≈ ${sharedFormatUsd(value)}"
