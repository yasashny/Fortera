package com.yasashny.fortera.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.managetokens.ManageTokens
import com.yasashny.fortera.feature.receive.SelectTokenForReceive
import com.yasashny.fortera.feature.receive.SelectTokenForSend
import com.yasashny.fortera.feature.tokendetails.TokenDetails
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
                MainContract.Effect.NavigateToManageTokens -> navigator.navigate(ManageTokens)
                MainContract.Effect.NavigateToSettings -> navigator.navigate(Settings)
                is MainContract.Effect.NavigateToTokenDetails -> navigator.navigate(TokenDetails(effect.tokenId))
                MainContract.Effect.NavigateToSelectTokenForReceive -> navigator.navigate(SelectTokenForReceive)
                MainContract.Effect.NavigateToSelectTokenForSend -> navigator.navigate(SelectTokenForSend)
            }
        },
    ) { state, sendIntent ->
        MainLayout(
            state = state,
            onWalletSelectorClick = { showWalletSelector = true },
            onSettingsClick = { sendIntent(MainContract.Intent.OpenSettings) },
            onManageTokensClick = { sendIntent(MainContract.Intent.OpenManageTokens) },
            onTokenClick = { tokenId -> sendIntent(MainContract.Intent.OpenTokenDetails(tokenId)) },
            onSendClick = { sendIntent(MainContract.Intent.OpenSend) },
            onReceiveClick = { sendIntent(MainContract.Intent.OpenReceive) },
            onRefresh = { sendIntent(MainContract.Intent.Refresh) },
        )
    }

    if (showWalletSelector) {
        WalletSelectorSheet(onDismiss = { showWalletSelector = false })
    }
}
