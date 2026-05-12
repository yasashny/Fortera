package com.yasashny.fortera.feature.managetokens.ui

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.managetokens.AddCustomToken
import com.yasashny.fortera.feature.managetokens.presentation.ManageTokensEffect
import com.yasashny.fortera.feature.managetokens.presentation.ManageTokensIntent
import com.yasashny.fortera.feature.managetokens.presentation.ManageTokensViewModel
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
                ManageTokensEffect.NavigateBack -> navigator.back()
            }
        },
    ) { state, sendIntent ->
        ManageTokensLayout(
            state = state,
            onBackClick = { navigator.back() },
            onAddClick = { navigator.navigate(AddCustomToken) },
            onToggle = { tokenId -> sendIntent(ManageTokensIntent.Toggle(tokenId)) },
        )
    }
}
