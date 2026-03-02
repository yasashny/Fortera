package com.yasashny.fortera.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.settings.SettingsContract.Effect
import com.yasashny.fortera.feature.settings.SettingsContract.Intent
import com.yasashny.fortera.feature.settings.SettingsContract.State

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>,
) : MviViewModel<State, Intent, Effect>(State()) {

    companion object {
        val USE_PASSWORD_KEY = booleanPreferencesKey("settings_use_password")
    }

    init {
        intent {
            launch {
                dataStore.data
                    .collect { prefs ->
                        reduce(State(isPasswordEnabled = prefs[USE_PASSWORD_KEY] == true, isLoading = false))
                    }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.TogglePassword -> onTogglePassword()
        }
    }

    private fun onTogglePassword() = intent {
        sendEffect(Effect.RequestBiometric(enable = !currentState.isPasswordEnabled))
    }

    fun onBiometricConfirmed(enable: Boolean) = intent {
        dataStore.edit { prefs -> prefs[USE_PASSWORD_KEY] = enable }
    }
}
