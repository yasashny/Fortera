package com.yasashny.fortera.core.cryptoapi.client

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CoinGeckoClient(private val okhttp: OkHttpClient) {

    // Returns map of coingeckoId → Pair(priceUsd, change24h)
    suspend fun getPrices(coingeckoIds: List<String>): Map<String, Pair<Double, Double>> {
        if (coingeckoIds.isEmpty()) return emptyMap()
        val ids = coingeckoIds.joinToString(",")
        val url = "https://api.coingecko.com/api/v3/simple/price?ids=$ids&vs_currencies=usd&include_24hr_change=true"
        val request = Request.Builder().url(url).get().build()
        val body = okhttp.newCall(request).execute().use { it.body!!.string() }
        val json = JSONObject(body)
        val result = mutableMapOf<String, Pair<Double, Double>>()
        for (id in coingeckoIds) {
            if (json.has(id)) {
                val obj = json.getJSONObject(id)
                val price = obj.optDouble("usd", 0.0)
                val change = obj.optDouble("usd_24h_change", 0.0)
                result[id] = price to change
            }
        }
        return result
    }
}
