package com.yasashny.fortera.feature.walletselector.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorViewModel
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val walletSelectorModule = module {
    single(named("walletSelector")) { WalletSelectorNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::WalletSelectorViewModel)
    viewModel { params ->
        WalletSettingsViewModel(
            walletId = params.get<String>(),
            walletInteractor = get()
        )
    }
}
