package com.yasashny.fortera.feature.receive.send

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.ConfirmSend
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SendScreen(
    tokenId: String,
    viewModel: SendViewModel = koinViewModel { parametersOf(tokenId) },
) {
    val navigator = LocalAppNavigator.current
    val context = LocalContext.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                SendContract.Effect.NavigateBack -> navigator.back()
                is SendContract.Effect.NavigateToConfirm -> navigator.navigate(
                    ConfirmSend(
                        tokenId = effect.tokenId,
                        amount = effect.amount,
                        address = effect.address,
                    )
                )
            }
        },
    ) { state, sendIntent ->
        SendLayout(
            state = state,
            onBackClick = { navigator.back() },
            onAmountChange = { sendIntent(SendContract.Intent.UpdateAmount(it)) },
            onAddressChange = { sendIntent(SendContract.Intent.UpdateAddress(it)) },
            onPasteClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return@SendLayout
                sendIntent(SendContract.Intent.UpdateAddress(clip))
            },
            onContinueClick = { sendIntent(SendContract.Intent.Continue) },
        )
    }
}
