package com.yasashny.fortera.feature.walletselector.settings.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.dialog.ErrorDialog
import com.yasashny.fortera.core.ui.text.asString
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract.Effect
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun WalletSettingsScreen(
    walletId: String,
    viewModel: WalletSettingsViewModel = koinViewModel { parametersOf(walletId) },
) {
    val navigator = LocalAppNavigator.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateBack -> navigator.back()
                is Effect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message.asString(context))
            }
        },
    ) { state, onIntent ->
        WalletSettingsLayout(
            state = state,
            onIntent = onIntent,
            snackbarHostState = snackbarHostState,
        )

        ErrorDialog(
            message = state.errorMessage,
            onDismiss = { onIntent(WalletSettingsContract.Intent.DismissError) },
        )
    }
}
