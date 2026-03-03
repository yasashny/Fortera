package com.yasashny.fortera.core.network.cache

internal data class CacheEntry<T>(
    val data: T,
    val cachedAt: Long = System.currentTimeMillis(),
)
