package com.yasashny.fortera.feature.settings

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState

object SettingsContract {

    data class State(
        val isPasswordEnabled: Boolean = false,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object TogglePassword : Intent
    }

    sealed interface Effect : UiEffect {
        data class RequestBiometric(val enable: Boolean) : Effect
    }
}
