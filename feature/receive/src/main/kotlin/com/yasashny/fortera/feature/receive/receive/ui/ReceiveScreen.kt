package com.yasashny.fortera.feature.receive.receive.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.receive.R as ReceiveR
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.yasashny.fortera.feature.receive.receive.presentation.ReceiveEffect
import com.yasashny.fortera.feature.receive.receive.presentation.ReceiveViewModel

@Composable
internal fun ReceiveScreen(
    tokenId: String,
    viewModel: ReceiveViewModel = koinViewModel { parametersOf(tokenId) },
) {
    val navigator = LocalAppNavigator.current
    val context = LocalContext.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                ReceiveEffect.NavigateBack -> navigator.back()
            }
        },
    ) { state, _ ->
        ReceiveLayout(
            state = state,
            onBackClick = { navigator.back() },
            onCopyAddress = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("address", state.address))
                Toast.makeText(
                    context,
                    context.getString(ReceiveR.string.receive_address_copied),
                    Toast.LENGTH_SHORT,
                ).show()
            },
        )
    }
}
