package com.yasashny.fortera.feature.startup.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.createwallet.CreateWallet
import com.yasashny.fortera.feature.importwallet.ImportWallet
import com.yasashny.fortera.feature.startup.presentation.StartupEffect
import com.yasashny.fortera.feature.startup.presentation.StartupViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun StartupScreen(
    modifier: Modifier = Modifier,
    viewModel: StartupViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current
    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                StartupEffect.NavigateToCreateWallet -> navigator.navigate(CreateWallet)
                StartupEffect.NavigateToImportWallet -> navigator.navigate(ImportWallet)
            }
        },
    ) { _, onIntent ->
        StartupLayout(
            onIntent = onIntent,
            modifier = modifier,
        )
    }
}
