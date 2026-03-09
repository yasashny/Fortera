package com.yasashny.fortera.core.domaincrypto.datasource

import com.yasashny.fortera.core.domaincrypto.BuildConfig
import com.yasashny.fortera.core.domaincrypto.model.Transaction
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

class InfuraDataSource(private val httpClient: HttpClient) {

    private val baseUrl = "https://mainnet.infura.io/v3/${BuildConfig.INFURA_PROJECT_ID}"

    suspend fun getEthBalance(address: String): BigDecimal {
        val body = """{"jsonrpc":"2.0","method":"eth_getBalance","params":["$address","latest"],"id":1}"""
        val response = post(body)
        val hex = JSONObject(response).getString("result")
        val wei = BigInteger(hex.removePrefix("0x"), 16)
        return wei.toBigDecimal().movePointLeft(18).stripTrailingZeros()
    }

    suspend fun getErc20Balance(address: String, contractAddress: String, decimals: Int = 18): BigDecimal {
        val paddedAddress = address.removePrefix("0x").lowercase().padStart(64, '0')
        val data = "0x70a08231$paddedAddress"
        val body = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$contractAddress","data":"$data"},"latest"],"id":1}"""
        val response = post(body)
        val hex = JSONObject(response).getString("result")
        if (hex == "0x" || hex.isBlank()) return BigDecimal.ZERO
        val raw = BigInteger(hex.removePrefix("0x"), 16)
        return raw.toBigDecimal().movePointLeft(decimals).stripTrailingZeros()
    }

    suspend fun getEthTransactions(address: String, limit: Int = 10): List<Transaction> {
        val url = "https://eth.blockscout.com/api/v2/addresses/$address/transactions"
        val response = httpClient.get(url).bodyAsText()
        val json = JSONObject(response)
        val items = json.optJSONArray("items") ?: return emptyList()
        val count = minOf(items.length(), limit)
        return (0 until count).mapNotNull { i ->
            runCatching {
                val tx = items.getJSONObject(i)
                val hash = tx.getString("hash")
                val timestamp = tx.optString("timestamp", "")
                val epochSeconds = if (timestamp.isNotEmpty()) {
                    Instant.parse(timestamp).epochSecond
                } else 0L
                val fromHash = tx.optJSONObject("from")?.optString("hash", "") ?: ""
                val toHash = tx.optJSONObject("to")?.optString("hash", "") ?: ""
                val valueWei = tx.optString("value", "0")
                val amount = BigInteger(valueWei).toBigDecimal().movePointLeft(18).stripTrailingZeros()
                val isIncoming = toHash.equals(address, ignoreCase = true)
                val confirmed = tx.optString("status", "") == "ok"

                Transaction(
                    hash = hash,
                    timestampSeconds = epochSeconds,
                    from = fromHash,
                    to = toHash,
                    amount = amount,
                    symbol = "ETH",
                    isIncoming = isIncoming,
                    confirmed = confirmed,
                )
            }.getOrNull()
        }
    }

    suspend fun getErc20Transactions(
        address: String,
        contractAddress: String,
        symbol: String,
        decimals: Int = 18,
        limit: Int = 10,
    ): List<Transaction> {
        val url = "https://eth.blockscout.com/api/v2/addresses/$address/token-transfers?type=ERC-20&token=$contractAddress"
        val response = httpClient.get(url).bodyAsText()
        val json = JSONObject(response)
        val items = json.optJSONArray("items") ?: return emptyList()
        val count = minOf(items.length(), limit)
        return (0 until count).mapNotNull { i ->
            runCatching {
                val tx = items.getJSONObject(i)
                val hash = tx.optJSONObject("tx_hash")?.optString("hash")
                    ?: tx.optString("transaction_hash", "")
                val timestamp = tx.optString("timestamp", "")
                val epochSeconds = if (timestamp.isNotEmpty()) {
                    Instant.parse(timestamp).epochSecond
                } else 0L
                val fromHash = tx.optJSONObject("from")?.optString("hash", "") ?: ""
                val toHash = tx.optJSONObject("to")?.optString("hash", "") ?: ""
                val totalValue = tx.optJSONObject("total")?.optString("value", "0") ?: "0"
                val amount = BigInteger(totalValue).toBigDecimal().movePointLeft(decimals).stripTrailingZeros()
                val isIncoming = toHash.equals(address, ignoreCase = true)

                Transaction(
                    hash = hash,
                    timestampSeconds = epochSeconds,
                    from = fromHash,
                    to = toHash,
                    amount = amount,
                    symbol = symbol,
                    isIncoming = isIncoming,
                    confirmed = true,
                )
            }.getOrNull()
        }
    }

    private suspend fun post(bodyStr: String): String {
        return httpClient.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(bodyStr)
        }.bodyAsText()
    }
}
