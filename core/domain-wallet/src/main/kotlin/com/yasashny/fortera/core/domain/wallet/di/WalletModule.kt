package com.yasashny.fortera.core.domain.wallet.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.yasashny.fortera.core.common.ForteraDispatchers
import com.yasashny.fortera.core.database.secure.SecureStorage
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domain.wallet.WalletRepository
import com.yasashny.fortera.core.domain.wallet.WalletRepositoryImpl
import com.yasashny.fortera.core.domain.wallet.db.WalletDao
import com.yasashny.fortera.core.domain.wallet.db.WalletDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val walletModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            WalletDatabase::class.java,
            "wallet.db",
        ).fallbackToDestructiveMigration().build()
    }

    single<WalletDao> { get<WalletDatabase>().walletDao() }

    single<WalletRepository> {
        WalletRepositoryImpl(
            walletDao = get<WalletDao>(),
            dataStore = get<DataStore<Preferences>>(),
            secureStorage = get<SecureStorage>(),
        )
    }

    factory { WalletInteractor(get(), get(named(ForteraDispatchers.IO))) }
}
