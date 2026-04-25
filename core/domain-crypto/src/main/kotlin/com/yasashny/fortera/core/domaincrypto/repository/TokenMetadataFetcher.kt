package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata

interface TokenMetadataFetcher {
    suspend fun fetch(input: String): Result<CustomTokenMetadata>
}
