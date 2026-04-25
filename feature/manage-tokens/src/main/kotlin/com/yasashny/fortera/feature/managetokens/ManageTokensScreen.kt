package com.yasashny.fortera.feature.managetokens

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageTokensScreen(
    viewModel: ManageTokensViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                ManageTokensContract.Effect.NavigateBack -> navigator.back()
            }
        },
    ) { state, sendIntent ->
        ManageTokensLayout(
            state = state,
            onBackClick = { navigator.back() },
            onAddClick = { navigator.navigate(AddCustomToken) },
            onToggle = { tokenId -> sendIntent(ManageTokensContract.Intent.Toggle(tokenId)) },
        )
    }
}
