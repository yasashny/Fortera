package com.yasashny.fortera.feature.importwallet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.dialog.ErrorDialog
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.Effect
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletViewModel
import com.yasashny.fortera.feature.main.Main
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ImportWalletScreen(
    modifier: Modifier = Modifier,
    viewModel: ImportWalletViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateBack -> navigator.back()
                Effect.NavigateToHome -> navigator.clearAndNavigate(Main)
            }
        },
    ) { state, onIntent ->
        ImportWalletLayout(
            state = state,
            onIntent = onIntent,
            modifier = modifier,
        )

        ErrorDialog(
            message = state.errorMessage,
            onDismiss = { onIntent(ImportWalletContract.Intent.DismissError) },
        )
    }
}
