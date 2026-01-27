package com.yasashny.fortera.feature.startup.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.startup.di.StartupNavProvider
import com.yasashny.fortera.feature.startup.presentation.StartupViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val startupModule = module {
    single(named("startup")) { StartupNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::StartupViewModel)
}
