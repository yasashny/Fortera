package com.yasashny.fortera.lock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yasashny.fortera.feature.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AuthState {
    data object Loading : AuthState
    data object Locked : AuthState
    data object Unlocked : AuthState
}

class AppLockViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)

    val authState: StateFlow<AuthState> = combine(
        dataStore.data.map { it[SettingsViewModel.USE_PASSWORD_KEY] ?: false },
        _isAuthenticated,
    ) { usePassword, isAuthenticated ->
        when {
            !usePassword || isAuthenticated -> AuthState.Unlocked
            else -> AuthState.Locked
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState.Loading,
    )

    fun onAuthenticated() {
        _isAuthenticated.value = true
    }
}
