package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata

/**
 * Resolves a user-entered string into full token metadata for the "add custom token" flow.
 *
 * The implementation auto-detects whether the input is an Ethereum contract address
 * (`0x` + 40 hex chars) or a coin-listing ID (e.g. `"shiba-inu"`). For addresses, an
 * RPC node is queried first and a public coin-index lookup is used as a fallback so
 * the flow still works when the node is rate-limited or unavailable.
 *
 * Returns a [Result] so callers can distinguish "input was rejected" from
 * "network blew up" at the UI layer.
 */
interface TokenMetadataFetcher {
    suspend fun fetch(input: String): Result<CustomTokenMetadata>
}
