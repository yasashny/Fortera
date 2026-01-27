package com.yasashny.fortera.feature.createwallet.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletContract.Effect
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletViewModel
import com.yasashny.fortera.feature.main.Main
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateWalletScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateWalletViewModel = koinViewModel()
) {
    val navigator = LocalAppNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateBack -> navigator.back()
                Effect.NavigateToMain -> navigator.clearAndNavigate(Main)
                is Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    ) { state, onIntent ->
        CreateWalletLayout(state = state, modifier = modifier, onIntent = onIntent)
    }
}