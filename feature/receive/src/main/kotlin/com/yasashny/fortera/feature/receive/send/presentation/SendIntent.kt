package com.yasashny.fortera.feature.receive.send.presentation

import com.yasashny.fortera.core.mvi.UiIntent

internal sealed interface SendIntent : UiIntent {
    data class UpdateAmount(val amount: String) : SendIntent
    data class UpdateAddress(val address: String) : SendIntent
    data object Continue : SendIntent
}
