package com.yasashny.fortera.feature.managetokens.di

import com.yasashny.fortera.core.common.ForteraDispatchers
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.managetokens.ManageTokensViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val manageTokensModule = module {
    single(named("manageTokens")) { ManageTokensNavProvider() } bind FeatureNavProvider::class
    viewModel { ManageTokensViewModel(get(), get(), get(named(ForteraDispatchers.IO))) }
}
