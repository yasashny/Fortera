package com.yasashny.fortera

import android.app.Application
import com.yasashny.fortera.core.common.dispatchersModule
import com.yasashny.fortera.core.domaincrypto.di.domainCryptoModule
import com.yasashny.fortera.core.network.di.networkModule
import com.yasashny.fortera.core.database.di.databaseModule
import com.yasashny.fortera.core.domain.wallet.di.walletModule
import com.yasashny.fortera.feature.walletselector.di.walletSelectorModule
import com.yasashny.fortera.feature.settings.di.settingsModule
import com.yasashny.fortera.feature.managetokens.di.manageTokensModule
import com.yasashny.fortera.feature.tokendetails.di.tokenDetailsModule
import com.yasashny.fortera.feature.receive.di.receiveModule
import com.yasashny.fortera.core.navigation.AppNavigator
import com.yasashny.fortera.core.navigation.NavigationRegistry
import com.yasashny.fortera.feature.createwallet.di.createWalletModule
import com.yasashny.fortera.feature.importwallet.di.importWalletModule
import com.yasashny.fortera.feature.main.di.mainModule
import com.yasashny.fortera.feature.startup.Startup
import com.yasashny.fortera.feature.startup.di.startupModule
import com.yasashny.fortera.lock.AppLockViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class ForteraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@ForteraApp)
            modules(
                dispatchersModule,
                databaseModule,
                networkModule,
                domainCryptoModule,
                walletModule,
                module {
                    single { AppNavigator(Startup) }
                    single { NavigationRegistry(getAll()) }
                    viewModelOf(::AppLockViewModel)
                },
                startupModule,
                importWalletModule,
                createWalletModule,
                mainModule,
                walletSelectorModule,
                settingsModule,
                manageTokensModule,
                tokenDetailsModule,
                receiveModule,
            )
        }
    }
}
