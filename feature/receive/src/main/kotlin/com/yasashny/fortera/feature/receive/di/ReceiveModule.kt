package com.yasashny.fortera.feature.receive.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.receive.receive.ReceiveViewModel
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val receiveModule = module {
    single(named("receive")) { ReceiveNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::SelectTokenViewModel)
    viewModel { params ->
        ReceiveViewModel(
            tokenId = params.get<String>(),
            walletInteractor = get(),
        )
    }
}
