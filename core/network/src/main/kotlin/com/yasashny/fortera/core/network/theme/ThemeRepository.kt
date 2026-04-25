package com.yasashny.fortera.core.network.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yasashny.fortera.core.common.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for the user's selected theme mode.
 *
 * Backed by [DataStore] — the selection survives process death and is observable by every
 * screen that depends on theme. Mirrors [com.yasashny.fortera.core.network.currency.CurrencyRepository]
 * in shape and contract; changes made through [set] propagate to all subscribers of [observe].
 */
interface ThemeRepository {
    fun observe(): Flow<ThemeMode>
    suspend fun current(): ThemeMode
    suspend fun set(mode: ThemeMode)
}

internal class ThemeRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ThemeRepository {

    private val key = stringPreferencesKey("app_theme_mode")

    override fun observe(): Flow<ThemeMode> =
        dataStore.data
            .map { prefs -> ThemeMode.fromNameOrDefault(prefs[key]) }
            .distinctUntilChanged()

    override suspend fun current(): ThemeMode = observe().first()

    override suspend fun set(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[key] = mode.name }
    }
}
