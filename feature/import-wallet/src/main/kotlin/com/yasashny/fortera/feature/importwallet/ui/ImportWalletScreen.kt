package com.yasashny.fortera.feature.importwallet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletContract.Effect
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletViewModel
import com.yasashny.fortera.feature.main.Main
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ImportWalletScreen(
    modifier: Modifier = Modifier,
    viewModel: ImportWalletViewModel = koinViewModel()
) {
    val navigator = LocalAppNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateBack -> navigator.back()
                Effect.NavigateToHome -> navigator.clearAndNavigate(Main)
                is Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                Effect.RequestPaste -> Unit
            }
        }
    ) { state, onIntent ->
        ImportWalletLayout(
            state = state,
            onIntent = onIntent,
            modifier = modifier,
        )
    }
}
