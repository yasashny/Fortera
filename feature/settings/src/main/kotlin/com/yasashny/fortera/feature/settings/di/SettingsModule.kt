package com.yasashny.fortera.feature.settings.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    single(named("settings")) { SettingsNavProvider() } bind FeatureNavProvider::class
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
}
