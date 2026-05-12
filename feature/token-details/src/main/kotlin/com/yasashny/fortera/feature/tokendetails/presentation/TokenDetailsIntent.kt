package com.yasashny.fortera.feature.tokendetails.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface TokenDetailsIntent : UiIntent {
    data class SelectPeriod(val period: ChartPeriod) : TokenDetailsIntent
    data object OpenReceive : TokenDetailsIntent
    data object OpenSend : TokenDetailsIntent
}
