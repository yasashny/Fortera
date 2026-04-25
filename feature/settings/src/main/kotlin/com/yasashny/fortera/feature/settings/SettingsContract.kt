package com.yasashny.fortera.feature.settings

import com.yasashny.fortera.core.common.currency.Currency
import com.yasashny.fortera.core.common.theme.ThemeMode
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.network.environment.AppEnvironment

object SettingsContract {

    data class State(
        val isPasswordEnabled: Boolean = false,
        val isLoading: Boolean = true,
        val environment: AppEnvironment = AppEnvironment.MAINNET,
        val isEnvSheetOpen: Boolean = false,
        val currency: Currency = Currency.Default,
        val isCurrencySheetOpen: Boolean = false,
        val themeMode: ThemeMode = ThemeMode.Default,
        val isThemeSheetOpen: Boolean = false,
        val isDebugMode: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object TogglePassword : Intent
        data object OpenEnvironmentSheet : Intent
        data object DismissEnvironmentSheet : Intent
        data class SelectEnvironment(val env: AppEnvironment) : Intent
        data object OpenCurrencySheet : Intent
        data object DismissCurrencySheet : Intent
        data class SelectCurrency(val currency: Currency) : Intent
        data object OpenThemeSheet : Intent
        data object DismissThemeSheet : Intent
        data class SelectTheme(val mode: ThemeMode) : Intent
    }

    sealed interface Effect : UiEffect {
        data class RequestBiometric(val enable: Boolean) : Effect
    }
}
