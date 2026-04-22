package com.yasashny.fortera.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.network.environment.AppEnvironment
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import com.yasashny.fortera.feature.settings.SettingsContract.Effect
import com.yasashny.fortera.feature.settings.SettingsContract.Intent
import com.yasashny.fortera.feature.settings.SettingsContract.State

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>,
    private val environmentRepository: EnvironmentRepository,
    private val balanceRepository: BalanceRepository,
) : MviViewModel<State, Intent, Effect>(State()) {

    companion object {
        val USE_PASSWORD_KEY = booleanPreferencesKey("settings_use_password")
    }

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
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.TogglePassword -> onTogglePassword()
            Intent.OpenEnvironmentSheet -> intent {
                reduce(currentState.copy(isEnvSheetOpen = true))
            }
            Intent.DismissEnvironmentSheet -> intent {
                reduce(currentState.copy(isEnvSheetOpen = false))
            }
            is Intent.SelectEnvironment -> intent {
                if (intent.env != currentState.environment) {
                    environmentRepository.set(intent.env)
                    balanceRepository.clearAllCaches()
                }
                reduce(currentState.copy(isEnvSheetOpen = false))
            }
        }
    }

    private fun onTogglePassword() = intent {
        sendEffect(Effect.RequestBiometric(enable = !currentState.isPasswordEnabled))
    }

    fun onBiometricConfirmed(enable: Boolean) = intent {
        dataStore.edit { prefs -> prefs[USE_PASSWORD_KEY] = enable }
    }
}
