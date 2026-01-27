package com.yasashny.fortera.feature.importwallet.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val importWalletModule = module {
    single(named("importWallet")) { ImportWalletNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::ImportWalletViewModel)
}
