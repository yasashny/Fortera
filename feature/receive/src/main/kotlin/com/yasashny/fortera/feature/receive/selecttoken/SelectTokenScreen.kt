package com.yasashny.fortera.feature.receive.selecttoken

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.ReceiveToken
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SelectTokenScreen(
    viewModel: SelectTokenViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is SelectTokenContract.Effect.NavigateToReceive ->
                    navigator.navigate(ReceiveToken(effect.tokenId))
            }
        },
    ) { state, sendIntent ->
        SelectTokenLayout(
            state = state,
            onBackClick = { navigator.back() },
            onTokenClick = { sendIntent(SelectTokenContract.Intent.SelectToken(it)) },
        )
    }
}
