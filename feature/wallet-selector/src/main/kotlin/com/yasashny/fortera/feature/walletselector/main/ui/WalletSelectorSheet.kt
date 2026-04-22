package com.yasashny.fortera.feature.walletselector.main.ui

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.dialog.ErrorDialog
import com.yasashny.fortera.feature.createwallet.CreateWallet
import com.yasashny.fortera.feature.importwallet.ImportWallet
import com.yasashny.fortera.feature.walletselector.WalletSettings
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract.Effect
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun WalletSelectorSheet(
    onDismiss: () -> Unit,
    viewModel: WalletSelectorViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateToCreateWallet -> {
                    onDismiss()
                    navigator.navigate(CreateWallet)
                }
                Effect.NavigateToImportWallet -> {
                    onDismiss()
                    navigator.navigate(ImportWallet)
                }
                is Effect.NavigateToSettings -> {
                    onDismiss()
                    navigator.navigate(WalletSettings(effect.walletId))
                }
            }
        },
    ) { state, onIntent ->
        WalletSelectorLayout(
            state = state,
            onIntent = onIntent,
            onDismiss = onDismiss,
        )

        ErrorDialog(
            message = state.errorMessage,
            onDismiss = { onIntent(WalletSelectorContract.Intent.DismissError) },
        )
    }
}
