package com.yasashny.fortera.feature.receive.send.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.ConfirmSend
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.yasashny.fortera.feature.receive.send.presentation.SendEffect
import com.yasashny.fortera.feature.receive.send.presentation.SendIntent
import com.yasashny.fortera.feature.receive.send.presentation.SendViewModel

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
                SendEffect.NavigateBack -> navigator.back()
                is SendEffect.NavigateToConfirm -> navigator.navigate(
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
            onAmountChange = { sendIntent(SendIntent.UpdateAmount(it)) },
            onAddressChange = { sendIntent(SendIntent.UpdateAddress(it)) },
            onPasteClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return@SendLayout
                sendIntent(SendIntent.UpdateAddress(clip))
            },
            onContinueClick = { sendIntent(SendIntent.Continue) },
        )
    }
}
