package com.yasashny.fortera.core.domaincrypto.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject
import java.math.BigDecimal

class BlockstreamDataSource(private val httpClient: HttpClient) {

    suspend fun getBtcBalance(address: String): BigDecimal {
        val response = httpClient.get("https://blockstream.info/api/address/$address").bodyAsText()
        val json = JSONObject(response)
        val chainStats = json.getJSONObject("chain_stats")
        val funded = chainStats.getLong("funded_txo_sum")
        val spent = chainStats.getLong("spent_txo_sum")
        val satoshis = funded - spent
        return satoshis.toBigDecimal().movePointLeft(8).stripTrailingZeros()
    }
}
