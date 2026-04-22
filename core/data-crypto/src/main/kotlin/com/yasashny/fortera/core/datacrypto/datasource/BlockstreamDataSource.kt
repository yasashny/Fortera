package com.yasashny.fortera.core.datacrypto.datasource

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.Transaction
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

class BlockstreamDataSource(
    private val httpClient: HttpClient,
    private val environmentRepository: EnvironmentRepository,
) {

    private suspend fun baseUrl(): String = environmentRepository.current().btcEsploraBase

    suspend fun getBtcBalance(address: String): BigDecimal {
        val response = httpClient.get("${baseUrl()}/address/$address").bodyAsText()
        val json = JSONObject(response)
        val chainStats = json.getJSONObject("chain_stats")
        val funded = chainStats.getLong("funded_txo_sum")
        val spent = chainStats.getLong("spent_txo_sum")
        val satoshis = funded - spent
        return satoshis.toBigDecimal().movePointLeft(8).stripTrailingZeros()
    }

    suspend fun getTransactions(address: String, limit: Int = 10): List<Transaction> {
        val response = httpClient.get("${baseUrl()}/address/$address/txs").bodyAsText()
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

            for (j in 0 until vout.length()) {
                val output = vout.getJSONObject(j)
                val outputAddress = output.optString("scriptpubkey_address", "")
                val value = output.optLong("value", 0L)
                if (outputAddress == address) {
                    receivedSatoshis += value
                }
            }

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
                symbol = BlockchainNetwork.BITCOIN.nativeSymbol,
                isIncoming = isIncoming,
                confirmed = confirmed,
            )
        }
    }

    suspend fun getFeeEstimates(): Map<Int, Double> {
        val response = httpClient.get("${baseUrl()}/fee-estimates").bodyAsText()
        val json = JSONObject(response)
        val out = mutableMapOf<Int, Double>()
        for (key in json.keys()) {
            val target = key.toIntOrNull() ?: continue
            out[target] = json.getDouble(key)
        }
        return out
    }

    suspend fun getUtxos(address: String): List<Utxo> {
        val response = httpClient.get("${baseUrl()}/address/$address/utxo").bodyAsText()
        val arr = JSONArray(response)
        return (0 until arr.length()).map { i ->
            val u = arr.getJSONObject(i)
            val status = u.getJSONObject("status")
            Utxo(
                txid = u.getString("txid"),
                vout = u.getInt("vout"),
                valueSats = u.getLong("value"),
                confirmed = status.optBoolean("confirmed", false),
                blockHeight = if (status.optBoolean("confirmed", false)) status.optInt("block_height") else null,
            )
        }
    }

    suspend fun broadcastTransaction(rawHex: String): String {
        val response = httpClient.post("${baseUrl()}/tx") {
            contentType(ContentType.Text.Plain)
            setBody(rawHex)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw RuntimeException("broadcast failed: $body")
        }
        return body.trim()
    }

    data class Utxo(
        val txid: String,
        val vout: Int,
        val valueSats: Long,
        val confirmed: Boolean,
        val blockHeight: Int?,
    )
}
