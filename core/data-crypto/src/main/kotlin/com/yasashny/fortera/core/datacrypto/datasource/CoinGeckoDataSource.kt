package com.yasashny.fortera.core.datacrypto.datasource

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

/**
 * Public CoinGecko v3 API. Used by the "add custom token" flow to resolve an
 * Ethereum contract address into name / symbol / decimals via the
 * `/coins/ethereum/contract/{address}` reverse-lookup endpoint.
 *
 * The free public endpoint has no auth but is rate-limited; the single-call,
 * on-demand usage here stays well within the limits.
 */
class CoinGeckoDataSource(private val httpClient: HttpClient) {

    suspend fun fetchByContract(contractAddress: String): CustomTokenMetadata? = runCatching {
        val url = "$BASE_URL/coins/$ETHEREUM_PLATFORM/contract/${contractAddress.lowercase()}"
        val json = JSONObject(httpClient.get(url).bodyAsText())
        if (!json.has("id") || json.has("error")) return null

        val coinId = json.optString("id").takeIf { it.isNotBlank() } ?: return null
        val name = json.optString("name").takeIf { it.isNotBlank() } ?: return null
        val symbol = json.optString("symbol").takeIf { it.isNotBlank() } ?: return null
        val detail = json.optJSONObject("detail_platforms")?.optJSONObject(ETHEREUM_PLATFORM)
            ?: return null
        val contract = detail.optString("contract_address").takeIf { it.isNotBlank() }
            ?: return null
        val decimals = detail.optInt("decimal_place", -1).takeIf { it >= 0 }
            ?: return null

        CustomTokenMetadata(
            name = name,
            symbol = symbol.uppercase(),
            decimals = decimals,
            contractAddress = contract.lowercase(),
            network = BlockchainNetwork.ETHEREUM,
            coingeckoId = coinId,
        )
    }.getOrNull()

    private companion object {
        const val BASE_URL = "https://api.coingecko.com/api/v3"
        const val ETHEREUM_PLATFORM = "ethereum"
    }
}
