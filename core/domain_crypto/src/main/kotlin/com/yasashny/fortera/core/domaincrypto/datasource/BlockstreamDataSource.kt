package com.yasashny.fortera.core.domaincrypto.datasource

import com.yasashny.fortera.core.domaincrypto.model.Transaction
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONArray
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

    suspend fun getTransactions(address: String, limit: Int = 10): List<Transaction> {
        val response = httpClient.get("https://blockstream.info/api/address/$address/txs").bodyAsText()
        val arr = JSONArray(response)
        val count = minOf(arr.length(), limit)
        return (0 until count).map { i ->
            val tx = arr.getJSONObject(i)
            val status = tx.getJSONObject("status")
            val confirmed = status.optBoolean("confirmed", false)
            val blockTime = status.optLong("block_time", 0L)

            val vout = tx.getJSONArray("vout")
            var receivedSatoshis = 0L
            var sentSatoshis = 0L

            // Check outputs: if our address is in vout, it's incoming value
            for (j in 0 until vout.length()) {
                val output = vout.getJSONObject(j)
                val outputAddress = output.optString("scriptpubkey_address", "")
                val value = output.optLong("value", 0L)
                if (outputAddress == address) {
                    receivedSatoshis += value
                }
            }

            // Check inputs: if our address funded an input, it's outgoing
            val vin = tx.getJSONArray("vin")
            for (j in 0 until vin.length()) {
                val input = vin.getJSONObject(j)
                val prevout = input.optJSONObject("prevout")
                if (prevout != null && prevout.optString("scriptpubkey_address", "") == address) {
                    sentSatoshis += prevout.optLong("value", 0L)
                }
            }

            val isIncoming = receivedSatoshis > sentSatoshis
            val netSatoshis = if (isIncoming) receivedSatoshis else sentSatoshis - receivedSatoshis
            val amount = netSatoshis.toBigDecimal().movePointLeft(8).stripTrailingZeros()

            // Determine counterparty
            val from: String
            val to: String
            if (isIncoming) {
                from = if (vin.length() > 0) {
                    vin.getJSONObject(0).optJSONObject("prevout")
                        ?.optString("scriptpubkey_address", "") ?: ""
                } else ""
                to = address
            } else {
                from = address
                to = (0 until vout.length())
                    .map { vout.getJSONObject(it).optString("scriptpubkey_address", "") }
                    .firstOrNull { it != address && it.isNotEmpty() } ?: ""
            }

            Transaction(
                hash = tx.getString("txid"),
                timestampSeconds = blockTime,
                from = from,
                to = to,
                amount = amount,
                symbol = "BTC",
                isIncoming = isIncoming,
                confirmed = confirmed,
            )
        }
    }
}
