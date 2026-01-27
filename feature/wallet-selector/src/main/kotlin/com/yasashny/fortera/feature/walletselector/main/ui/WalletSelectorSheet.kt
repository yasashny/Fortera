package com.yasashny.fortera.feature.walletselector.main.ui

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.createwallet.CreateWallet
import com.yasashny.fortera.feature.importwallet.ImportWallet
import com.yasashny.fortera.feature.walletselector.WalletSettings
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
            onDismiss()
            when (effect) {
                Effect.NavigateToCreateWallet -> navigator.navigate(CreateWallet)
                Effect.NavigateToImportWallet -> navigator.navigate(ImportWallet)
                is Effect.NavigateToSettings -> navigator.navigate(WalletSettings(effect.walletId))
                is Effect.ShowError -> { /* snackbar not available here, ignore */
                }
            }
        },
    ) { state, onIntent ->
        WalletSelectorLayout(
            state = state,
            onIntent = onIntent,
            onDismiss = onDismiss,
        )
    }
}
