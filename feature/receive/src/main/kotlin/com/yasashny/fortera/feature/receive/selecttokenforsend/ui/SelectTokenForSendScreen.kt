package com.yasashny.fortera.feature.receive.selecttokenforsend.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.SendToken
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenEffect
import com.yasashny.fortera.feature.receive.selecttoken.presentation.SelectTokenIntent
import com.yasashny.fortera.feature.receive.selecttoken.ui.SelectTokenLayout
import com.yasashny.fortera.feature.receive.selecttokenforsend.presentation.SelectTokenForSendViewModel
import org.koin.androidx.compose.koinViewModel
import com.yasashny.fortera.feature.receive.R as ReceiveR

@Composable
internal fun SelectTokenForSendScreen(
    viewModel: SelectTokenForSendViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is SelectTokenEffect.NavigateToReceive ->
                    navigator.navigate(SendToken(effect.tokenId))
            }
        },
    ) { state, sendIntent ->
        SelectTokenLayout(
            title = stringResource(ReceiveR.string.send_title),
            state = state,
            onBackClick = { navigator.back() },
            onTokenClick = { sendIntent(SelectTokenIntent.SelectToken(it)) },
        )
    }
}
