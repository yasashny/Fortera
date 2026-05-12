package com.yasashny.fortera.feature.receive.selecttoken.presentation

import com.yasashny.fortera.core.mvi.UiIntent

internal sealed interface SelectTokenIntent : UiIntent {
    data class SelectToken(val tokenId: String) : SelectTokenIntent
}
