package com.yasashny.fortera.feature.tokendetails

import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import java.math.BigDecimal

object TokenDetailsContract {

    enum class ChartPeriod(val days: String, val label: String) {
        DAY("24h", "1D"),
        WEEK("1w", "1W"),
        MONTH("1m", "1M"),
        YEAR("1y", "1Y"),
        ALL("all", "All"),
    }

    data class State(
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val balance: BigDecimal = BigDecimal.ZERO,
        val priceUsd: Double = 0.0,
        val changePercent24h: Double = 0.0,
        val priceHistory: List<PricePoint> = emptyList(),
        val selectedPeriod: ChartPeriod = ChartPeriod.WEEK,
        val isLoading: Boolean = true,
        val isChartLoading: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class SelectPeriod(val period: ChartPeriod) : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
    }
}
