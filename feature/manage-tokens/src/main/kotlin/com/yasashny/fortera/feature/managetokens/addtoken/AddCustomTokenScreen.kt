package com.yasashny.fortera.feature.managetokens.addtoken

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import com.yasashny.fortera.core.ui.text.asString
import com.yasashny.fortera.feature.managetokens.addtoken.AddCustomTokenContract.Effect
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
                Effect.NavigateBack -> navigator.back()
                is Effect.ShowError -> snackbarHostState.showSnackbar(effect.message.asString(context))
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
