package com.yasashny.fortera.core.cryptoapi.client

import com.yasashny.fortera.core.cryptoapi.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger

class InfuraClient(private val okhttp: OkHttpClient) {

    private val baseUrl = "https://mainnet.infura.io/v3/${BuildConfig.INFURA_PROJECT_ID}"
    private val json = "application/json".toMediaType()

    suspend fun getEthBalance(address: String): BigDecimal {
        val body = """{"jsonrpc":"2.0","method":"eth_getBalance","params":["$address","latest"],"id":1}"""
        val response = post(body)
        val hex = JSONObject(response).getString("result")
        val wei = BigInteger(hex.removePrefix("0x"), 16)
        return wei.toBigDecimal().movePointLeft(18).stripTrailingZeros()
    }

    suspend fun getErc20Balance(address: String, contractAddress: String, decimals: Int = 18): BigDecimal {
        // balanceOf(address) selector: 0x70a08231
        val paddedAddress = address.removePrefix("0x").lowercase().padStart(64, '0')
        val data = "0x70a08231$paddedAddress"
        val body = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$contractAddress","data":"$data"},"latest"],"id":1}"""
        val response = post(body)
        val hex = JSONObject(response).getString("result")
        if (hex == "0x" || hex.isBlank()) return BigDecimal.ZERO
        val raw = BigInteger(hex.removePrefix("0x"), 16)
        return raw.toBigDecimal().movePointLeft(decimals).stripTrailingZeros()
    }

    private fun post(bodyStr: String): String {
        val request = Request.Builder()
            .url(baseUrl)
            .post(bodyStr.toRequestBody(json))
            .build()
        return okhttp.newCall(request).execute().use { it.body!!.string() }
    }
}
