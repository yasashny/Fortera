package com.yasashny.fortera.feature.receive.confirmsend

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.dialog.ErrorDialog
import com.yasashny.fortera.feature.receive.SendSuccess
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ConfirmSendScreen(
    tokenId: String,
    amount: String,
    address: String,
    viewModel: ConfirmSendViewModel = koinViewModel { parametersOf(tokenId, amount, address) },
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                ConfirmSendContract.Effect.NavigateBack -> navigator.back()
                ConfirmSendContract.Effect.NavigateToSuccess -> navigator.navigate(SendSuccess)
            }
        },
    ) { state, sendIntent ->
        ConfirmSendLayout(
            state = state,
            onBackClick = { navigator.back() },
            onSendClick = { sendIntent(ConfirmSendContract.Intent.Send) },
            onSpeedClick = { sendIntent(ConfirmSendContract.Intent.OpenSpeedSheet) },
            onDismissSpeedSheet = { sendIntent(ConfirmSendContract.Intent.DismissSpeedSheet) },
            onSelectSpeed = { sendIntent(ConfirmSendContract.Intent.SelectSpeed(it)) },
        )

        ErrorDialog(
            message = state.errorMessage,
            onDismiss = { sendIntent(ConfirmSendContract.Intent.DismissError) },
        )
    }
}
