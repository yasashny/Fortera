package com.yasashny.fortera.feature.main.di

import com.yasashny.fortera.core.common.ForteraDispatchers
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.main.domain.MainOverviewInteractor
import com.yasashny.fortera.feature.main.presentation.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    single(named("main")) { MainNavProvider() } bind FeatureNavProvider::class

    factory {
        MainOverviewInteractor(
            walletInteractor = get(),
            walletBalances = get(),
            dispatcher = get(named(ForteraDispatchers.IO)),
        )
    }

    viewModelOf(::MainViewModel)
}
