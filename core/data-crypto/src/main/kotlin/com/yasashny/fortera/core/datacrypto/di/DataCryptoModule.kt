package com.yasashny.fortera.core.datacrypto.di

import androidx.room.Room
import com.yasashny.fortera.core.datacrypto.AddressResolverImpl
import com.yasashny.fortera.core.datacrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.datacrypto.datasource.CoinGeckoDataSource
import com.yasashny.fortera.core.datacrypto.datasource.CoinStatsDataSource
import com.yasashny.fortera.core.datacrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.datacrypto.db.TokenDatabase
import com.yasashny.fortera.core.datacrypto.repository.AddressValidatorImpl
import com.yasashny.fortera.core.datacrypto.repository.BalanceRepositoryImpl
import com.yasashny.fortera.core.datacrypto.repository.PriceRepositoryImpl
import com.yasashny.fortera.core.datacrypto.repository.SendTransactionRepositoryImpl
import com.yasashny.fortera.core.datacrypto.repository.TokenMetadataFetcherImpl
import com.yasashny.fortera.core.datacrypto.repository.TokenRepositoryImpl
import com.yasashny.fortera.core.datacrypto.repository.TransactionRepositoryImpl
import com.yasashny.fortera.core.datacrypto.repository.send.BitcoinSender
import com.yasashny.fortera.core.datacrypto.repository.send.EthereumSender
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import com.yasashny.fortera.core.domaincrypto.repository.AddressValidator
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenMetadataFetcher
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepository
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val dataCryptoModule = module {
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
    single { CoinGeckoDataSource(get<HttpClient>()) }
    single { TokenMetadataFetcherImpl(get()) } bind TokenMetadataFetcher::class
    single { AddressResolverImpl(get()) } bind AddressResolver::class
    single { AddressValidatorImpl(get()) } bind AddressValidator::class
    single { PriceRepositoryImpl(get(), get()) } bind PriceRepository::class
    single { BalanceRepositoryImpl(get(), get(), get(), get(), get()) } bind BalanceRepository::class
    single { TransactionRepositoryImpl(get(), get()) } bind TransactionRepository::class
    single { EthereumSender(get(), get<AddressResolverImpl>(), get()) }
    single { BitcoinSender(get(), get<AddressResolverImpl>()) }
    single { SendTransactionRepositoryImpl(get(), get()) } bind SendTransactionRepository::class
}
