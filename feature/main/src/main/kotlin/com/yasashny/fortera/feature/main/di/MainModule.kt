package com.yasashny.fortera.feature.main.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.main.MainNavProvider
import com.yasashny.fortera.feature.main.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    single(named("main")) { MainNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::MainViewModel)
}
