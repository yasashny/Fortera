package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.PriceInfo
import com.yasashny.fortera.core.domaincrypto.model.PricePoint

interface PriceRepository {
    suspend fun getPrices(
        coinIds: List<String>,
        policy: FetchPolicy = FetchPolicy.CacheFirst,
    ): Map<String, PriceInfo>

    suspend fun getChart(coinId: String, period: String): List<PricePoint>
}
