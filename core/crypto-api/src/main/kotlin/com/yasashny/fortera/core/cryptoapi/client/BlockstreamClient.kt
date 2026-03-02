package com.yasashny.fortera.core.cryptoapi.client

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal

class BlockstreamClient(private val okhttp: OkHttpClient) {

    suspend fun getBtcBalance(address: String): BigDecimal {
        val request = Request.Builder()
            .url("https://blockstream.info/api/address/$address")
            .get()
            .build()
        val body = okhttp.newCall(request).execute().use { it.body!!.string() }
        val json = JSONObject(body)
        val chainStats = json.getJSONObject("chain_stats")
        val funded = chainStats.getLong("funded_txo_sum")
        val spent = chainStats.getLong("spent_txo_sum")
        val satoshis = funded - spent
        return satoshis.toBigDecimal().movePointLeft(8).stripTrailingZeros()
    }
}
