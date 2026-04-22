package com.yasashny.fortera.core.walletbalances.di

import com.yasashny.fortera.core.common.ForteraDispatchers
import com.yasashny.fortera.core.walletbalances.WalletAddressesService
import com.yasashny.fortera.core.walletbalances.WalletAddressesServiceImpl
import com.yasashny.fortera.core.walletbalances.WalletBalances
import com.yasashny.fortera.core.walletbalances.WalletBalancesImpl
import com.yasashny.fortera.core.walletbalances.WalletTransactionSender
import com.yasashny.fortera.core.walletbalances.WalletTransactionSenderImpl
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val walletBalancesModule = module {
    single { WalletAddressesServiceImpl(get(), get()) } bind WalletAddressesService::class
    single {
        WalletBalancesImpl(
            walletInteractor = get(),
            tokenRepository = get(),
            balanceRepository = get(),
            addresses = get(),
            dispatcher = get(named(ForteraDispatchers.IO)),
        )
    } bind WalletBalances::class
    single { WalletTransactionSenderImpl(get(), get()) } bind WalletTransactionSender::class
}
