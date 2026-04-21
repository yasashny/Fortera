package com.yasashny.fortera.core.domaincrypto.di

import androidx.room.Room
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import com.yasashny.fortera.core.domaincrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.CoinStatsDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.db.TokenDatabase
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepositoryImpl
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepositoryImpl
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepositoryImpl
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepository
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepositoryImpl
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepository
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepositoryImpl
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val domainCryptoModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TokenDatabase::class.java,
            "token.db",
        ).fallbackToDestructiveMigration().build()
    }
    single { get<TokenDatabase>().tokenDao() }
    single { TokenRepositoryImpl(get()) } bind TokenRepository::class
    single { InfuraDataSource(get<HttpClient>(), get()) }
    single { BlockstreamDataSource(get<HttpClient>(), get()) }
    single { CoinStatsDataSource(get<HttpClient>()) }
    single { AddressResolver(get()) }
    single { PriceRepositoryImpl(get(), get()) } bind PriceRepository::class
    single { BalanceRepositoryImpl(get(), get(), get(), get(), get()) } bind BalanceRepository::class
    single { TransactionRepositoryImpl(get(), get()) } bind TransactionRepository::class
    single { SendTransactionRepositoryImpl(get(), get(), get(), get()) } bind SendTransactionRepository::class
}
