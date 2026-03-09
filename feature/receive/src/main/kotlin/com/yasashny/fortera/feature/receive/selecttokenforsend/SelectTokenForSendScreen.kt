package com.yasashny.fortera.feature.receive.selecttokenforsend

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.R as ReceiveR
import com.yasashny.fortera.feature.receive.SendToken
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenContract
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenLayout
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SelectTokenForSendScreen(
    viewModel: SelectTokenForSendViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is SelectTokenContract.Effect.NavigateToReceive ->
                    navigator.navigate(SendToken(effect.tokenId))
            }
        },
    ) { state, sendIntent ->
        SelectTokenLayout(
            title = stringResource(ReceiveR.string.send_title),
            state = state,
            onBackClick = { navigator.back() },
            onTokenClick = { sendIntent(SelectTokenContract.Intent.SelectToken(it)) },
        )
    }
}
