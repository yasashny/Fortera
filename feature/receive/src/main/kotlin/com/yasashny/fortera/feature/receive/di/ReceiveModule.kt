package com.yasashny.fortera.feature.receive.di

import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendViewModel
import com.yasashny.fortera.feature.receive.receive.ReceiveViewModel
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenViewModel
import com.yasashny.fortera.feature.receive.selecttokenforsend.SelectTokenForSendViewModel
import com.yasashny.fortera.feature.receive.send.SendViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val receiveModule = module {
    single(named("receive")) { ReceiveNavProvider() } bind FeatureNavProvider::class
    viewModelOf(::SelectTokenViewModel)
    viewModelOf(::SelectTokenForSendViewModel)
    viewModel { params ->
        ReceiveViewModel(
            tokenId = params.get<String>(),
            walletInteractor = get(),
            tokenRepository = get(),
            addressResolver = get(),
        )
    }
    viewModel { params ->
        SendViewModel(
            tokenId = params.get<String>(),
            walletInteractor = get(),
            balanceRepository = get(),
            tokenRepository = get(),
            addressResolver = get(),
        )
    }
    viewModel { params ->
        ConfirmSendViewModel(
            tokenId = params.get<String>(0),
            amount = params.get<String>(1),
            address = params.get<String>(2),
            walletInteractor = get(),
            priceRepository = get(),
            tokenRepository = get(),
            sendTransactionRepository = get(),
        )
    }
}
