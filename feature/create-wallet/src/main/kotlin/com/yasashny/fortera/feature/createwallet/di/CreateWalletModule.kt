package com.yasashny.fortera.feature.createwallet.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val createWalletModule = module {
    single(named("createWallet")) { CreateWalletNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::CreateWalletViewModel)
}
