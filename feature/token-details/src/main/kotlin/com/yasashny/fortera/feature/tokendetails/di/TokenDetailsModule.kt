package com.yasashny.fortera.feature.tokendetails.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.tokendetails.TokenDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val tokenDetailsModule = module {
    single(named("tokenDetails")) { TokenDetailsNavProvider() } bind FeatureNavProvider::class
    viewModel { params ->
        TokenDetailsViewModel(
            tokenId = params.get<String>(),
            walletInteractor = get(),
            balanceRepository = get(),
            priceRepository = get(),
            transactionRepository = get(),
        )
    }
}
