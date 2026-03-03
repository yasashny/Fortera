package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.datasource.CoinStatsDataSource
import com.yasashny.fortera.core.domaincrypto.model.PriceInfo
import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import com.yasashny.fortera.core.network.cache.InMemoryCache

interface PriceRepository {
    fun getPricesLocal(coinIds: List<String>): Map<String, PriceInfo>?
    suspend fun getPricesRemote(coinIds: List<String>): Map<String, PriceInfo>
    suspend fun getPricesLocalOrRemote(coinIds: List<String>): Map<String, PriceInfo>
    suspend fun getChart(coinId: String, period: String): List<PricePoint>
}

internal class PriceRepositoryImpl(
    private val coinStatsDataSource: CoinStatsDataSource,
    private val cache: InMemoryCache,
) : PriceRepository {

    override fun getPricesLocal(coinIds: List<String>): Map<String, PriceInfo>? {
        val key = priceKey(coinIds)
        return cache.getLocal(key)
    }

    override suspend fun getPricesRemote(coinIds: List<String>): Map<String, PriceInfo> {
        val key = priceKey(coinIds)
        return cache.getRemote(key) { coinStatsDataSource.getPrices(coinIds) }
    }

    override suspend fun getPricesLocalOrRemote(coinIds: List<String>): Map<String, PriceInfo> {
        val key = priceKey(coinIds)
        return cache.getLocalOrRemote(key) { coinStatsDataSource.getPrices(coinIds) }
    }

    override suspend fun getChart(coinId: String, period: String): List<PricePoint> {
        val key = "chart:$coinId:$period"
        return cache.getLocalOrRemote(key) { coinStatsDataSource.getChart(coinId, period) }
    }

    private fun priceKey(ids: List<String>): String = "prices:${ids.sorted().joinToString(",")}"
}
