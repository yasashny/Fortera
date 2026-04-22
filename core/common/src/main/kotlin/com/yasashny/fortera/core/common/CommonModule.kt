package com.yasashny.fortera.core.common

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    single { AndroidHaptics(androidContext()) } bind Haptics::class
}
