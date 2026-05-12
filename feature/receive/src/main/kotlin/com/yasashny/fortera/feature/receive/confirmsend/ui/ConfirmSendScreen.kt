package com.yasashny.fortera.feature.receive.confirmsend.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.dialog.ErrorDialog
import com.yasashny.fortera.feature.receive.R
import com.yasashny.fortera.feature.receive.SendSuccess
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.yasashny.fortera.feature.receive.confirmsend.presentation.ConfirmSendEffect
import com.yasashny.fortera.feature.receive.confirmsend.presentation.ConfirmSendIntent
import com.yasashny.fortera.feature.receive.confirmsend.presentation.ConfirmSendViewModel

@Composable
internal fun ConfirmSendScreen(
    tokenId: String,
    amount: String,
    address: String,
    viewModel: ConfirmSendViewModel = koinViewModel { parametersOf(tokenId, amount, address) },
) {
    val navigator = LocalAppNavigator.current
    val context = LocalContext.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                ConfirmSendEffect.NavigateBack -> navigator.back()
                is ConfirmSendEffect.NavigateToSuccess ->
                    navigator.navigate(SendSuccess(tokenId, effect.amount, effect.symbol))
            }
        },
    ) { state, sendIntent ->
        ConfirmSendLayout(
            state = state,
            onBackClick = { navigator.back() },
            onSendClick = { sendIntent(ConfirmSendIntent.Send) },
            onSpeedClick = { sendIntent(ConfirmSendIntent.OpenSpeedSheet) },
            onDismissSpeedSheet = { sendIntent(ConfirmSendIntent.DismissSpeedSheet) },
            onSelectSpeed = { sendIntent(ConfirmSendIntent.SelectSpeed(it)) },
            onAddressClick = { sendIntent(ConfirmSendIntent.OpenAddressSheet) },
            onDismissAddressSheet = { sendIntent(ConfirmSendIntent.DismissAddressSheet) },
            onCopyAddress = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("address", state.address))
                Toast.makeText(
                    context,
                    context.getString(R.string.send_confirm_address_copied),
                    Toast.LENGTH_SHORT,
                ).show()
                sendIntent(ConfirmSendIntent.DismissAddressSheet)
            },
        )

        ErrorDialog(
            message = state.errorMessage,
            onDismiss = { sendIntent(ConfirmSendIntent.DismissError) },
        )
    }
}
