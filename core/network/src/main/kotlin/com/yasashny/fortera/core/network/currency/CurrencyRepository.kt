package com.yasashny.fortera.core.network.currency

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yasashny.fortera.core.common.currency.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for the user's selected display currency.
 *
 * Backed by [DataStore] — the selection survives process death and is observable by every
 * screen that shows fiat values. Mirrors [com.yasashny.fortera.core.network.environment.EnvironmentRepository]
 * in shape and contract; changes made through [set] propagate to all subscribers of [observe].
 */
interface CurrencyRepository {
    fun observe(): Flow<Currency>
    suspend fun current(): Currency
    suspend fun set(currency: Currency)
}

internal class CurrencyRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : CurrencyRepository {

    private val key = stringPreferencesKey("app_currency")

    override fun observe(): Flow<Currency> =
        dataStore.data
            .map { prefs -> Currency.fromCodeOrDefault(prefs[key]) }
            .distinctUntilChanged()

    override suspend fun current(): Currency = observe().first()

    override suspend fun set(currency: Currency) {
        dataStore.edit { prefs -> prefs[key] = currency.code }
    }
}
