package com.yasashny.fortera.feature.settings.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface SettingsEffect : UiEffect {
    data class RequestBiometric(val enable: Boolean) : SettingsEffect
}
