package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.PriceInfo
import com.yasashny.fortera.core.domaincrypto.model.PricePoint

interface PriceRepository {
    /** Fetch prices according to [policy]. CacheFirst is the default for idle reads. */
    suspend fun getPrices(
        coinIds: List<String>,
        policy: FetchPolicy = FetchPolicy.CacheFirst,
    ): Map<String, PriceInfo>

    /** Price chart for a single coin over a given period (e.g. "24h"). Always goes through the cache. */
    suspend fun getChart(coinId: String, period: String): List<PricePoint>
}
