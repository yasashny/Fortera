package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.datacrypto.datasource.CoinStatsDataSource
import com.yasashny.fortera.core.domaincrypto.model.PriceInfo
import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import com.yasashny.fortera.core.domaincrypto.repository.FetchPolicy
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.network.cache.InMemoryCache

internal class PriceRepositoryImpl(
    private val coinStatsDataSource: CoinStatsDataSource,
    private val cache: InMemoryCache,
) : PriceRepository {

    override suspend fun getPrices(
        coinIds: List<String>,
        policy: FetchPolicy,
    ): Map<String, PriceInfo> {
        val key = priceKey(coinIds)
        return when (policy) {
            FetchPolicy.RemoteOnly -> cache.getRemote(key) { coinStatsDataSource.getPrices(coinIds) }
            FetchPolicy.CacheFirst -> cache.getLocalOrRemote(key) { coinStatsDataSource.getPrices(coinIds) }
        }
    }

    override suspend fun getChart(coinId: String, period: String): List<PricePoint> {
        val key = "chart:$coinId:$period"
        return cache.getLocalOrRemote(key) { coinStatsDataSource.getChart(coinId, period) }
    }

    private fun priceKey(ids: List<String>): String = "prices:${ids.sorted().joinToString(",")}"
}
