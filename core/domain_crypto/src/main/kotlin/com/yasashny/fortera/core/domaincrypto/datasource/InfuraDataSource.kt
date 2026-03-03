package com.yasashny.fortera.core.domaincrypto.datasource

import com.yasashny.fortera.core.domaincrypto.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger

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

    private suspend fun post(bodyStr: String): String {
        return httpClient.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(bodyStr)
        }.bodyAsText()
    }
}
