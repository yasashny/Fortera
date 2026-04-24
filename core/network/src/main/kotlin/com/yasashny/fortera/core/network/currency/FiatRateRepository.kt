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

/**
 * A snapshot of USD→currency rates at a point in time.
 *
 * [ratesFromUsd] — multiplier to apply to a USD amount to get the target currency. A missing
 * entry means "we don't know yet" — [rateFor] returns `null`, and the UI is expected to render
 * a loading placeholder (shimmer) rather than a made-up value. There are deliberately no
 * bootstrap or fallback rates anywhere in the codebase: either we have a live rate fetched
 * from the API or persisted from a previous session, or we wait.
 *
 * [lastUpdatedMs] — `System.currentTimeMillis()` at the moment the snapshot was fetched.
 * Zero means "never fetched" — [isStale] always returns true in that case.
 */
data class FiatRates(
    val ratesFromUsd: Map<Currency, Double>,
    val lastUpdatedMs: Long,
) {
    /** `null` when the rate for [currency] hasn't been fetched yet. */
    fun rateFor(currency: Currency): Double? = ratesFromUsd[currency]

    fun isStale(
        thresholdMs: Long = DEFAULT_STALE_MS,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean = nowMs - lastUpdatedMs >= thresholdMs

    companion object {
        val Empty: FiatRates = FiatRates(ratesFromUsd = emptyMap(), lastUpdatedMs = 0L)

        /** Six hours. Rates update roughly daily — 6h gives us four chances per day. */
        val DEFAULT_STALE_MS: Long = TimeUnit.HOURS.toMillis(6)
    }
}

/**
 * Persisted USD→fiat conversion rates, backed by a free forex API.
 *
 * Rates are observed from DataStore so any screen under [com.yasashny.fortera.core.ui.currency.LocalFiat]
 * recomposes when they change. [refreshIfStale] is the single place that hits the network —
 * the caller (typically `MainActivity` on launch) decides when to trigger it; the repository
 * short-circuits if the persisted snapshot is still within the staleness window.
 */
interface FiatRateRepository {
    fun observeRates(): Flow<FiatRates>
    suspend fun current(): FiatRates
    suspend fun refreshIfStale(): Result<FiatRates>
}

/**
 * Uses `open.er-api.com` — free, no API key, ECB-style daily rates. The endpoint returns
 * rates for ~160 currencies; we filter to the four we care about at parse time so growth of
 * the API's coverage doesn't bloat our on-disk cache. Failures leave the persisted snapshot
 * intact — callers keep rendering with last-known rates rather than falling back to 1:1.
 */
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
