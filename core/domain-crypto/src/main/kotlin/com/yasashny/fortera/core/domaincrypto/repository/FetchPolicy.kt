package com.yasashny.fortera.core.domaincrypto.repository

/**
 * Controls how a repository fulfils a request relative to its cache.
 *
 * - [CacheFirst] — serve cached data when available, fall back to network otherwise.
 *   The default for idle reads.
 * - [RemoteOnly] — bypass the cache entirely; always hit the network.
 *   Use for pull-to-refresh and after mutating actions where the caller needs freshness guarantees.
 */
enum class FetchPolicy {
    CacheFirst,
    RemoteOnly,
}
