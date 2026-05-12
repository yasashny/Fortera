package com.yasashny.fortera.feature.receive.selecttoken.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.ReceiveToken
import com.yasashny.fortera.feature.receive.R as ReceiveR
import org.koin.androidx.compose.koinViewModel
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenEffect
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenIntent
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenViewModel

@Composable
internal fun SelectTokenScreen(
    viewModel: SelectTokenViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is SelectTokenEffect.NavigateToReceive ->
                    navigator.navigate(ReceiveToken(effect.tokenId))
            }
        },
    ) { state, sendIntent ->
        SelectTokenLayout(
            title = stringResource(ReceiveR.string.select_token_title),
            state = state,
            onBackClick = { navigator.back() },
            onTokenClick = { sendIntent(SelectTokenIntent.SelectToken(it)) },
        )
    }
}
