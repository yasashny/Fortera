package com.yasashny.fortera.core.network.currency

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yasashny.fortera.core.common.currency.Currency
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FiatRates(
    val ratesFromUsd: Map<Currency, Double>,
    val lastUpdatedMs: Long,
) {
    fun rateFor(currency: Currency): Double? = ratesFromUsd[currency]

    fun isStale(
        thresholdMs: Long = DEFAULT_STALE_MS,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean = nowMs - lastUpdatedMs >= thresholdMs

    companion object {
        val Empty: FiatRates = FiatRates(ratesFromUsd = emptyMap(), lastUpdatedMs = 0L)

        val DEFAULT_STALE_MS: Long = TimeUnit.HOURS.toMillis(6)
    }
}

interface FiatRateRepository {
    fun observeRates(): Flow<FiatRates>
    suspend fun current(): FiatRates
    suspend fun refreshIfStale(): Result<FiatRates>
}

internal class FiatRateRepositoryImpl(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>,
) : FiatRateRepository {

    override fun observeRates(): Flow<FiatRates> = dataStore.data
        .map { prefs ->
            FiatRates(
                ratesFromUsd = decode(prefs[RATES_KEY]),
                lastUpdatedMs = prefs[UPDATED_AT_KEY] ?: 0L,
            )
        }
        .distinctUntilChanged()

    override suspend fun current(): FiatRates = observeRates().first()

    override suspend fun refreshIfStale(): Result<FiatRates> = runCatching {
        val snapshot = current()
        if (!snapshot.isStale()) return@runCatching snapshot
        val fresh = fetch()
        dataStore.edit { prefs ->
            prefs[RATES_KEY] = encode(fresh.ratesFromUsd)
            prefs[UPDATED_AT_KEY] = fresh.lastUpdatedMs
        }
        fresh
    }

    private suspend fun fetch(): FiatRates {
        val body = httpClient.get(ENDPOINT).bodyAsText()
        val json = JSONObject(body)
        val result = json.optString("result")
        require(result == "success") {
            "fiat rate fetch failed: ${json.optString("error-type", result.ifEmpty { "unknown" })}"
        }
        val ratesJson = json.getJSONObject("rates")
        val rates = Currency.entries.mapNotNull { currency ->
            val rate = ratesJson.optDouble(currency.code, Double.NaN)
            if (rate.isNaN()) null else currency to rate
        }.toMap()
        return FiatRates(rates, System.currentTimeMillis())
    }

    private fun encode(rates: Map<Currency, Double>): String {
        val obj = JSONObject()
        rates.forEach { (currency, rate) -> obj.put(currency.code, rate) }
        return obj.toString()
    }

    private fun decode(stored: String?): Map<Currency, Double> {
        if (stored.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(stored)
            Currency.entries.mapNotNull { currency ->
                val rate = obj.optDouble(currency.code, Double.NaN)
                if (rate.isNaN()) null else currency to rate
            }.toMap()
        }.getOrElse { emptyMap() }
    }

    private companion object {
        const val ENDPOINT = "https://open.er-api.com/v6/latest/USD"
        val RATES_KEY = stringPreferencesKey("fiat_rates_json")
        val UPDATED_AT_KEY = longPreferencesKey("fiat_rates_updated_ms")
    }
}
