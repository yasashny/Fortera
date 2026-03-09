package com.yasashny.fortera.core.domaincrypto.di

import com.yasashny.fortera.core.domaincrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.CoinStatsDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepositoryImpl
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepositoryImpl
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepository
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepositoryImpl
import io.ktor.client.HttpClient
import org.koin.dsl.bind
import org.koin.dsl.module

val domainCryptoModule = module {
    single { InfuraDataSource(get<HttpClient>()) }
    single { BlockstreamDataSource(get<HttpClient>()) }
    single { CoinStatsDataSource(get<HttpClient>()) }
    single { PriceRepositoryImpl(get(), get()) } bind PriceRepository::class
    single { BalanceRepositoryImpl(get(), get(), get()) } bind BalanceRepository::class
    single { TransactionRepositoryImpl(get(), get()) } bind TransactionRepository::class
}
