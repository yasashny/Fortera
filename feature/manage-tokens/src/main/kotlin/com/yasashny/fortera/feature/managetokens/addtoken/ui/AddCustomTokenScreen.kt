package com.yasashny.fortera.feature.managetokens.addtoken.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import com.yasashny.fortera.core.ui.text.asString
import com.yasashny.fortera.feature.managetokens.addtoken.presentation.AddCustomTokenEffect
import com.yasashny.fortera.feature.managetokens.addtoken.presentation.AddCustomTokenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddCustomTokenScreen(
    viewModel: AddCustomTokenViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                AddCustomTokenEffect.NavigateBack -> navigator.back()
                is AddCustomTokenEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.asString(context))
            }
        },
    ) { state, sendIntent ->
        AddCustomTokenLayout(
            state = state,
            onIntent = sendIntent,
            onBackClick = { navigator.back() },
        )
    }
}
