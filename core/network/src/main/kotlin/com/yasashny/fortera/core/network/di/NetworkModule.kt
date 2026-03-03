package com.yasashny.fortera.core.network.di

import com.yasashny.fortera.core.network.HttpClientFactory
import com.yasashny.fortera.core.network.cache.InMemoryCache
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create() }
    single { InMemoryCache() }
}
