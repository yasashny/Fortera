package com.yasashny.fortera.core.network.cache

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class InMemoryCache(private val ttl: Duration = 1.days) {

    private val store = ConcurrentHashMap<String, CacheEntry<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getLocal(key: String): T? {
        val entry = store[key] ?: return null
        val age = System.currentTimeMillis() - entry.cachedAt
        if (age > ttl.inWholeMilliseconds) {
            store.remove(key)
            return null
        }
        return entry.data as T
    }

    suspend fun <T : Any> getRemote(key: String, fetcher: suspend () -> T): T {
        val data = fetcher()
        store[key] = CacheEntry(data)
        return data
    }

    suspend fun <T : Any> getLocalOrRemote(key: String, fetcher: suspend () -> T): T {
        return getLocal(key) ?: getRemote(key, fetcher)
    }

    fun invalidate(key: String) {
        store.remove(key)
    }

    fun invalidateAll() {
        store.clear()
    }
}
