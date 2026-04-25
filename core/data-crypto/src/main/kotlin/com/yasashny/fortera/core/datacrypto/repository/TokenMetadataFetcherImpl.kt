package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.datacrypto.datasource.CoinGeckoDataSource
import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.domaincrypto.repository.TokenMetadataFetcher

internal class TokenMetadataFetcherImpl(
    private val coinGecko: CoinGeckoDataSource,
) : TokenMetadataFetcher {

    override suspend fun fetch(input: String): Result<CustomTokenMetadata> = runCatching {
        val trimmed = input.trim()
        require(ETH_ADDRESS_REGEX.matches(trimmed)) { INVALID_ADDRESS }
        coinGecko.fetchByContract(trimmed.lowercase()) ?: error(NOT_FOUND)
    }

    private companion object {
        val ETH_ADDRESS_REGEX = Regex("^0x[a-fA-F0-9]{40}$")
        const val INVALID_ADDRESS = "invalid_address"
        const val NOT_FOUND = "not_found"
    }
}
