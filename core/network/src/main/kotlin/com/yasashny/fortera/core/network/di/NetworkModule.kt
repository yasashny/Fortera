package com.yasashny.fortera.core.network.di

import com.yasashny.fortera.core.network.HttpClientFactory
import com.yasashny.fortera.core.network.cache.InMemoryCache
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import com.yasashny.fortera.core.network.environment.EnvironmentRepositoryImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create() }
    single { InMemoryCache() }
    single { EnvironmentRepositoryImpl(get()) } bind EnvironmentRepository::class
}
