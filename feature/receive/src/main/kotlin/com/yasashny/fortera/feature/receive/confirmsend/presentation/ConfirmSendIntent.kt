package com.yasashny.fortera.feature.receive.confirmsend.presentation

import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.mvi.UiIntent

internal sealed interface ConfirmSendIntent : UiIntent {
    data object Send : ConfirmSendIntent
    data object OpenSpeedSheet : ConfirmSendIntent
    data object DismissSpeedSheet : ConfirmSendIntent
    data object OpenAddressSheet : ConfirmSendIntent
    data object DismissAddressSheet : ConfirmSendIntent
    data class SelectSpeed(val speed: FeeSpeed) : ConfirmSendIntent
    data object DismissError : ConfirmSendIntent
}
