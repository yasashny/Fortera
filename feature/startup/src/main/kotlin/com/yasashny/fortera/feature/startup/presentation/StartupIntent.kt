package com.yasashny.fortera.feature.startup.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface StartupIntent : UiIntent {
    data object CreateWalletClicked : StartupIntent
    data object ImportWalletClicked : StartupIntent
}
