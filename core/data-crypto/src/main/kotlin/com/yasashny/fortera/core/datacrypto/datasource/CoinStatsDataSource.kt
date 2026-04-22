package com.yasashny.fortera.core.datacrypto.datasource

import com.yasashny.fortera.core.datacrypto.BuildConfig
import com.yasashny.fortera.core.domaincrypto.model.PriceInfo
import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject

class CoinStatsDataSource(private val httpClient: HttpClient) {

    suspend fun getPrices(coinIds: List<String>): Map<String, PriceInfo> = coroutineScope {
        coinIds.map { coinId ->
            async {
                runCatching {
                    val url = "$BASE_URL/coins/$coinId"
                    val response = httpClient.get(url) {
                        header("X-API-KEY", BuildConfig.COINSTATS_API_KEY)
                    }.bodyAsText()
                    val json = JSONObject(response)
                    coinId to PriceInfo(
                        priceUsd = json.optDouble("price", 0.0),
                        changePercent24h = json.optDouble("priceChange1d", 0.0),
                    )
                }.getOrNull()
            }
        }.awaitAll().filterNotNull().toMap()
    }

    suspend fun getChart(coinId: String, period: String): List<PricePoint> {
        val url = "$BASE_URL/coins/$coinId/charts?period=$period"
        val response = httpClient.get(url) {
            header("X-API-KEY", BuildConfig.COINSTATS_API_KEY)
        }.bodyAsText()
        val arr = JSONArray(response)
        return (0 until arr.length()).map { i ->
            val point = arr.getJSONArray(i)
            PricePoint(
                timestampMs = point.getLong(0) * 1000,
                priceUsd = point.getDouble(1),
            )
        }
    }

    private companion object {
        const val BASE_URL = "https://openapiv1.coinstats.app"
    }
}
