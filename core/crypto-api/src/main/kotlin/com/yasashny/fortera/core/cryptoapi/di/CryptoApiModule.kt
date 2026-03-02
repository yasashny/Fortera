package com.yasashny.fortera.core.cryptoapi.di

import com.yasashny.fortera.core.common.ForteraDispatchers
import com.yasashny.fortera.core.cryptoapi.CryptoRepository
import com.yasashny.fortera.core.cryptoapi.CryptoRepositoryImpl
import com.yasashny.fortera.core.cryptoapi.client.BlockstreamClient
import com.yasashny.fortera.core.cryptoapi.client.CoinGeckoClient
import com.yasashny.fortera.core.cryptoapi.client.InfuraClient
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val cryptoApiModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single { InfuraClient(get()) }
    single { BlockstreamClient(get()) }
    single { CoinGeckoClient(get()) }
    single {
        CryptoRepositoryImpl(get(), get(), get(), get(named(ForteraDispatchers.IO)))
    } bind CryptoRepository::class
}
