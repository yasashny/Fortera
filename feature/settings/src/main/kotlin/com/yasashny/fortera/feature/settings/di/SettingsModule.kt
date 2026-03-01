package com.yasashny.fortera.feature.settings.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    single(named("settings")) { SettingsNavProvider() } bind FeatureNavProvider::class
}
