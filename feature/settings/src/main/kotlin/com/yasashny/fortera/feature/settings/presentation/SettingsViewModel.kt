package com.yasashny.fortera.feature.settings.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.network.currency.CurrencyRepository
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import com.yasashny.fortera.core.network.theme.ThemeRepository
import com.yasashny.fortera.feature.settings.BuildConfig

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>,
    private val environmentRepository: EnvironmentRepository,
    private val currencyRepository: CurrencyRepository,
    private val themeRepository: ThemeRepository,
    private val balanceRepository: BalanceRepository,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    init {
        intent {
            launch {
                dataStore.data.collect { prefs ->
                    reduce(
                        currentState.copy(
                            isPasswordEnabled = prefs[USE_PASSWORD_KEY] == true,
                            isLoading = false,
                            isDebugMode = BuildConfig.DEBUG,
                        )
                    )
                }
            }
            launch {
                environmentRepository.observe().collect { env ->
                    reduce(currentState.copy(environment = env))
                }
            }
            launch {
                currencyRepository.observe().collect { currency ->
                    reduce(currentState.copy(currency = currency))
                }
            }
            launch {
                themeRepository.observe().collect { mode ->
                    reduce(currentState.copy(themeMode = mode))
                }
            }
        }
    }

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.TogglePassword -> onTogglePassword()
            SettingsIntent.OpenEnvironmentSheet ->
                updateState { it.copy(isEnvSheetOpen = true) }
            SettingsIntent.DismissEnvironmentSheet ->
                updateState { it.copy(isEnvSheetOpen = false) }
            is SettingsIntent.SelectEnvironment -> intent {
                if (intent.env != currentState.environment) {
                    environmentRepository.set(intent.env)
                    balanceRepository.clearAllCaches()
                }
                reduce(currentState.copy(isEnvSheetOpen = false))
            }

            SettingsIntent.OpenCurrencySheet ->
                updateState { it.copy(isCurrencySheetOpen = true) }
            SettingsIntent.DismissCurrencySheet ->
                updateState { it.copy(isCurrencySheetOpen = false) }
            is SettingsIntent.SelectCurrency -> intent {
                if (intent.currency != currentState.currency) {
                    currencyRepository.set(intent.currency)
                }
                reduce(currentState.copy(isCurrencySheetOpen = false))
            }

            SettingsIntent.OpenThemeSheet ->
                updateState { it.copy(isThemeSheetOpen = true) }
            SettingsIntent.DismissThemeSheet ->
                updateState { it.copy(isThemeSheetOpen = false) }
            is SettingsIntent.SelectTheme -> intent {
                if (intent.mode != currentState.themeMode) {
                    themeRepository.set(intent.mode)
                }
                reduce(currentState.copy(isThemeSheetOpen = false))
            }
        }
    }

    private fun onTogglePassword() {
        sendEffect(SettingsEffect.RequestBiometric(enable = !currentState.isPasswordEnabled))
    }

    fun onBiometricConfirmed(enable: Boolean) = intent {
        dataStore.edit { prefs -> prefs[USE_PASSWORD_KEY] = enable }
    }

    companion object {
        val USE_PASSWORD_KEY = booleanPreferencesKey("settings_use_password")
    }
}
