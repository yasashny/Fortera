package com.yasashny.fortera.feature.settings.presentation

import com.yasashny.fortera.core.common.currency.Currency
import com.yasashny.fortera.core.common.theme.ThemeMode
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.network.environment.AppEnvironment

sealed interface SettingsIntent : UiIntent {
    data object TogglePassword : SettingsIntent

    data object OpenEnvironmentSheet : SettingsIntent
    data object DismissEnvironmentSheet : SettingsIntent
    data class SelectEnvironment(val env: AppEnvironment) : SettingsIntent

    data object OpenCurrencySheet : SettingsIntent
    data object DismissCurrencySheet : SettingsIntent
    data class SelectCurrency(val currency: Currency) : SettingsIntent

    data object OpenThemeSheet : SettingsIntent
    data object DismissThemeSheet : SettingsIntent
    data class SelectTheme(val mode: ThemeMode) : SettingsIntent
}
