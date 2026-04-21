package com.yasashny.fortera.core.network.environment

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface EnvironmentRepository {
    fun observe(): Flow<AppEnvironment>
    suspend fun current(): AppEnvironment
    suspend fun set(env: AppEnvironment)
}

internal class EnvironmentRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : EnvironmentRepository {

    private val key = stringPreferencesKey("app_environment")

    override fun observe(): Flow<AppEnvironment> =
        dataStore.data
            .map { prefs ->
                prefs[key]
                    ?.let { name -> runCatching { AppEnvironment.valueOf(name) }.getOrNull() }
                    ?: AppEnvironment.MAINNET
            }
            .distinctUntilChanged()

    override suspend fun current(): AppEnvironment = observe().first()

    override suspend fun set(env: AppEnvironment) {
        dataStore.edit { prefs -> prefs[key] = env.name }
    }
}
