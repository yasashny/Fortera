package com.yasashny.fortera.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

enum class ForteraDispatchers {
    Main,
    IO,
    Default
}

val dispatchersModule = module {
    single<CoroutineDispatcher>(named(ForteraDispatchers.Main)) { Dispatchers.Main }
    single<CoroutineDispatcher>(named(ForteraDispatchers.IO)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(ForteraDispatchers.Default)) { Dispatchers.Default }
}
