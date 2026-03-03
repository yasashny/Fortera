package com.yasashny.fortera.feature.tokendetails

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
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
                TokenDetailsContract.Effect.NavigateBack -> navigator.back()
            }
        },
    ) { state, sendIntent ->
        TokenDetailsLayout(
            state = state,
            onBackClick = { navigator.back() },
            onPeriodSelected = { sendIntent(TokenDetailsContract.Intent.SelectPeriod(it)) },
        )
    }
}
