package com.yasashny.fortera.core.network.cache

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class InMemoryCacheTest {

    @Test
    fun `getLocal returns null for unknown key`() {
        val cache = InMemoryCache()

        val result: String? = cache.getLocal("missing")

        assertNull(result)
    }

    @Test
    fun `getRemote stores the fetched value and returns it`() = runTest {
        val cache = InMemoryCache()
        var fetched = 0

        val value = cache.getRemote("key") {
            fetched++
            "hello"
        }

        assertEquals("hello", value)
        assertEquals(1, fetched)
        assertEquals("hello", cache.getLocal<String>("key"))
    }

    @Test
    fun `getLocalOrRemote returns cached value when present and skips fetcher`() = runTest {
        val cache = InMemoryCache()
        cache.getRemote("key") { "first" }
        var calls = 0

        val value = cache.getLocalOrRemote("key") {
            calls++
            "second"
        }

        assertEquals("first", value)
        assertEquals(0, calls)
    }

    @Test
    fun `getLocalOrRemote falls back to fetcher when no cached value`() = runTest {
        val cache = InMemoryCache()

        val value = cache.getLocalOrRemote("key") { "fresh" }

        assertEquals("fresh", value)
        assertEquals("fresh", cache.getLocal<String>("key"))
    }

    @Test
    fun `invalidate removes the entry for a key`() = runTest {
        val cache = InMemoryCache()
        cache.getRemote("key") { 42 }

        cache.invalidate("key")

        assertNull(cache.getLocal<Int>("key"))
    }

    @Test
    fun `invalidateAll clears every entry`() = runTest {
        val cache = InMemoryCache()
        cache.getRemote("a") { "1" }
        cache.getRemote("b") { "2" }

        cache.invalidateAll()

        assertNull(cache.getLocal<String>("a"))
        assertNull(cache.getLocal<String>("b"))
    }

    @Test
    fun `expired entries are evicted on getLocal`() = runTest {
        val cache = InMemoryCache(ttl = 5.milliseconds)
        cache.getRemote("key") { "value" }
        Thread.sleep(20)

        val result: String? = cache.getLocal("key")

        assertNull(result)
        // Subsequent reads still see it as missing (eviction happened).
        assertNull(cache.getLocal<String>("key"))
    }

    @Test
    fun `getLocalOrRemote re-fetches after expiry`() = runTest {
        val cache = InMemoryCache(ttl = 5.milliseconds)
        cache.getLocalOrRemote("key") { "stale" }
        Thread.sleep(20)
        var calls = 0

        val value = cache.getLocalOrRemote("key") {
            calls++
            "fresh"
        }

        assertEquals("fresh", value)
        assertEquals(1, calls)
    }
}
