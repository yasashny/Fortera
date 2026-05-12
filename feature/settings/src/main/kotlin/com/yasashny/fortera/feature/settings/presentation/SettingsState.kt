package com.yasashny.fortera.feature.settings.presentation

import com.yasashny.fortera.core.common.currency.Currency
import com.yasashny.fortera.core.common.theme.ThemeMode
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.network.environment.AppEnvironment

data class SettingsState(
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
