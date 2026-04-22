package com.yasashny.fortera.core.datacrypto.datasource

import com.yasashny.fortera.core.datacrypto.BuildConfig
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
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

class InfuraDataSource(
    private val httpClient: HttpClient,
    private val environmentRepository: EnvironmentRepository,
) {

    private suspend fun rpcUrl(): String {
        val env = environmentRepository.current()
        return "https://${env.ethHost}/v3/${BuildConfig.INFURA_PROJECT_ID}"
    }

    private suspend fun blockscoutBase(): String = environmentRepository.current().ethBlockscoutBase

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
        val url = "${blockscoutBase()}/api/v2/addresses/$address/transactions"
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
                    symbol = BlockchainNetwork.ETHEREUM.nativeSymbol,
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
        val url = "${blockscoutBase()}/api/v2/addresses/$address/token-transfers?type=ERC-20&token=$contractAddress"
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

    suspend fun getGasPrice(): BigInteger {
        val body = """{"jsonrpc":"2.0","method":"eth_gasPrice","params":[],"id":1}"""
        val hex = JSONObject(post(body)).getString("result")
        return BigInteger(hex.removePrefix("0x"), 16)
    }

    /**
     * EIP-1559 fee history. Returns base fees per block (including the predicted next one)
     * and priority-fee samples at the requested [rewardPercentiles].
     *
     * @param blockCount number of recent blocks to sample (max 1024 on most providers)
     * @param rewardPercentiles percentiles in 0..100, e.g. `listOf(10, 50, 90)` for slow/medium/fast tiers
     */
    suspend fun getFeeHistory(
        blockCount: Int,
        rewardPercentiles: List<Int>,
    ): FeeHistory {
        val blockCountHex = "0x${blockCount.toString(16)}"
        val percentilesJson = rewardPercentiles.joinToString(",")
        val body = """{"jsonrpc":"2.0","method":"eth_feeHistory","params":["$blockCountHex","latest",[$percentilesJson]],"id":1}"""
        val json = JSONObject(post(body)).getJSONObject("result")

        val baseFees = json.getJSONArray("baseFeePerGas").let { arr ->
            (0 until arr.length()).map { BigInteger(arr.getString(it).removePrefix("0x"), 16) }
        }
        val rewards = json.optJSONArray("reward")?.let { arr ->
            (0 until arr.length()).map { i ->
                val row = arr.getJSONArray(i)
                (0 until row.length()).map { j ->
                    BigInteger(row.getString(j).removePrefix("0x"), 16)
                }
            }
        } ?: emptyList()

        return FeeHistory(baseFeePerGas = baseFees, rewardsPerBlock = rewards)
    }

    /**
     * Simulates a transaction on the node and returns the gas it would consume.
     * Used to avoid hard-coding ERC-20 transfer gas limits.
     */
    suspend fun estimateGas(
        from: String,
        to: String,
        data: String? = null,
        value: BigInteger = BigInteger.ZERO,
    ): BigInteger {
        val params = buildString {
            append("""{"from":"$from","to":"$to"""")
            if (value > BigInteger.ZERO) append(""","value":"0x${value.toString(16)}"""")
            if (data != null) append(""","data":"$data"""")
            append("}")
        }
        val body = """{"jsonrpc":"2.0","method":"eth_estimateGas","params":[$params,"latest"],"id":1}"""
        val response = JSONObject(post(body))
        response.optJSONObject("error")?.let {
            throw RuntimeException("eth_estimateGas: ${it.optString("message")}")
        }
        val hex = response.getString("result")
        return BigInteger(hex.removePrefix("0x"), 16)
    }

    suspend fun getNonce(address: String): BigInteger {
        val body = """{"jsonrpc":"2.0","method":"eth_getTransactionCount","params":["$address","pending"],"id":1}"""
        val hex = JSONObject(post(body)).getString("result")
        return BigInteger(hex.removePrefix("0x"), 16)
    }

    suspend fun sendRawTransaction(rawHex: String): String {
        val prefixed = if (rawHex.startsWith("0x")) rawHex else "0x$rawHex"
        val body = """{"jsonrpc":"2.0","method":"eth_sendRawTransaction","params":["$prefixed"],"id":1}"""
        val response = JSONObject(post(body))
        response.optJSONObject("error")?.let {
            throw RuntimeException("eth_sendRawTransaction: ${it.optString("message")}")
        }
        return response.getString("result")
    }

    private suspend fun post(bodyStr: String): String {
        return httpClient.post(rpcUrl()) {
            contentType(ContentType.Application.Json)
            setBody(bodyStr)
        }.bodyAsText()
    }

    /**
     * Snapshot returned by [getFeeHistory]. [baseFeePerGas] has `blockCount + 1` entries —
     * the last one is the node's prediction for the next block's base fee. [rewardsPerBlock]
     * has one entry per sampled block, each containing priority-fee samples at the requested
     * percentiles in the same order as passed in.
     */
    data class FeeHistory(
        val baseFeePerGas: List<BigInteger>,
        val rewardsPerBlock: List<List<BigInteger>>,
    ) {
        /** Node's predicted base fee for the next block (last element of [baseFeePerGas]). */
        val nextBaseFee: BigInteger
            get() = baseFeePerGas.lastOrNull() ?: BigInteger.ZERO

        /**
         * Median priority-fee tip across sampled blocks at the given percentile index.
         * [percentileIndex] refers to position in the `rewardPercentiles` list passed to
         * [getFeeHistory], not the percentile value itself.
         */
        fun medianPriorityFee(percentileIndex: Int): BigInteger {
            val samples = rewardsPerBlock.mapNotNull { row -> row.getOrNull(percentileIndex) }
                .filter { it > BigInteger.ZERO }
                .sorted()
            if (samples.isEmpty()) return BigInteger.ZERO
            val mid = samples.size / 2
            return if (samples.size % 2 == 1) samples[mid]
            else (samples[mid - 1] + samples[mid]).shiftRight(1)
        }
    }
}
