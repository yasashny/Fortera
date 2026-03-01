package com.yasashny.fortera.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.settings.Settings
import com.yasashny.fortera.feature.startup.Startup
import com.yasashny.fortera.feature.walletselector.main.ui.WalletSelectorSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
) {
    val navigator = LocalAppNavigator.current
    var showWalletSelector by remember { mutableStateOf(false) }

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                MainContract.Effect.NavigateToStartup -> navigator.clearAndNavigate(Startup)
            }
        },
    ) { state, _ ->
        MainLayout(
            state = state,
            onWalletSelectorClick = { showWalletSelector = true },
            onSettingsClick = { navigator.navigate(Settings) },
        )
    }

    if (showWalletSelector) {
        WalletSelectorSheet(onDismiss = { showWalletSelector = false })
    }
}
