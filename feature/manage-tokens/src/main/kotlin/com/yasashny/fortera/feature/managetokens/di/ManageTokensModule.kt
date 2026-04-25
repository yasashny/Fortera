package com.yasashny.fortera.feature.managetokens.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.managetokens.ManageTokensViewModel
import com.yasashny.fortera.feature.managetokens.addtoken.AddCustomTokenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val manageTokensModule = module {
    single(named("manageTokens")) { ManageTokensNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::ManageTokensViewModel)
    viewModelOf(::AddCustomTokenViewModel)
}
