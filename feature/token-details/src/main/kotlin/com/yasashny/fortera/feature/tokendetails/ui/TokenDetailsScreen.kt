package com.yasashny.fortera.feature.tokendetails.ui

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.ReceiveToken
import com.yasashny.fortera.feature.receive.SendToken
import com.yasashny.fortera.feature.tokendetails.presentation.TokenDetailsEffect
import com.yasashny.fortera.feature.tokendetails.presentation.TokenDetailsIntent
import com.yasashny.fortera.feature.tokendetails.presentation.TokenDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TokenDetailsScreen(
    tokenId: String,
    viewModel: TokenDetailsViewModel = koinViewModel { parametersOf(tokenId) },
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                TokenDetailsEffect.NavigateBack -> navigator.back()
                is TokenDetailsEffect.NavigateToReceive -> navigator.navigate(ReceiveToken(effect.tokenId))
                is TokenDetailsEffect.NavigateToSend -> navigator.navigate(SendToken(effect.tokenId))
            }
        },
    ) { state, sendIntent ->
        TokenDetailsLayout(
            state = state,
            onBackClick = { navigator.back() },
            onPeriodSelected = { sendIntent(TokenDetailsIntent.SelectPeriod(it)) },
            onSendClick = { sendIntent(TokenDetailsIntent.OpenSend) },
            onReceiveClick = { sendIntent(TokenDetailsIntent.OpenReceive) },
        )
    }
}
