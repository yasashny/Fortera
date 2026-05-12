package com.yasashny.fortera.feature.tokendetails.presentation

import androidx.annotation.StringRes
import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import com.yasashny.fortera.core.domaincrypto.model.Transaction
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.feature.tokendetails.R
import java.math.BigDecimal

data class TokenDetailsState(
    val tokenName: String = "",
    val tokenSymbol: String = "",
    val tokenIconUrl: String = "",
    val tokenContractAddress: String? = null,
    val balance: BigDecimal = BigDecimal.ZERO,
    val priceUsd: Double = 0.0,
    val changePercent24h: Double = 0.0,
    val priceHistory: List<PricePoint> = emptyList(),
    val selectedPeriod: ChartPeriod = ChartPeriod.WEEK,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val isChartLoading: Boolean = false,
    val isTransactionsLoading: Boolean = false,
) : UiState

enum class ChartPeriod(val days: String, @StringRes val labelRes: Int) {
    DAY("24h", R.string.token_details_period_1d),
    WEEK("1w", R.string.token_details_period_1w),
    MONTH("1m", R.string.token_details_period_1m),
    YEAR("1y", R.string.token_details_period_1y),
    ALL("all", R.string.token_details_period_all),
}
