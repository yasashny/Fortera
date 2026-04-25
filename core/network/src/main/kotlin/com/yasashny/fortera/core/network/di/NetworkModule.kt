package com.yasashny.fortera.core.network.di

import com.yasashny.fortera.core.network.HttpClientFactory
import com.yasashny.fortera.core.network.cache.InMemoryCache
import com.yasashny.fortera.core.network.currency.CurrencyRepository
import com.yasashny.fortera.core.network.currency.CurrencyRepositoryImpl
import com.yasashny.fortera.core.network.currency.FiatRateRepository
import com.yasashny.fortera.core.network.currency.FiatRateRepositoryImpl
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import com.yasashny.fortera.core.network.environment.EnvironmentRepositoryImpl
import com.yasashny.fortera.core.network.theme.ThemeRepository
import com.yasashny.fortera.core.network.theme.ThemeRepositoryImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create() }
    single { InMemoryCache() }
    single { EnvironmentRepositoryImpl(get()) } bind EnvironmentRepository::class
    single { CurrencyRepositoryImpl(get()) } bind CurrencyRepository::class
    single { FiatRateRepositoryImpl(get(), get()) } bind FiatRateRepository::class
    single { ThemeRepositoryImpl(get()) } bind ThemeRepository::class
}
