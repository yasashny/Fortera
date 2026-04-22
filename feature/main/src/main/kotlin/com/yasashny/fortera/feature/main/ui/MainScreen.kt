package com.yasashny.fortera.feature.main.ui

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.main.presentation.MainEffect
import com.yasashny.fortera.feature.main.presentation.MainIntent
import com.yasashny.fortera.feature.main.presentation.MainViewModel
import com.yasashny.fortera.feature.managetokens.ManageTokens
import com.yasashny.fortera.feature.receive.SelectTokenForReceive
import com.yasashny.fortera.feature.receive.SelectTokenForSend
import com.yasashny.fortera.feature.settings.Settings
import com.yasashny.fortera.feature.startup.Startup
import com.yasashny.fortera.feature.tokendetails.TokenDetails
import com.yasashny.fortera.feature.walletselector.main.ui.WalletSelectorSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val navigator = LocalAppNavigator.current

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                MainEffect.NavigateToStartup -> navigator.clearAndNavigate(Startup)
                MainEffect.NavigateToSettings -> navigator.navigate(Settings)
                MainEffect.NavigateToManageTokens -> navigator.navigate(ManageTokens)
                MainEffect.NavigateToSend -> navigator.navigate(SelectTokenForSend)
                MainEffect.NavigateToReceive -> navigator.navigate(SelectTokenForReceive)
                is MainEffect.NavigateToTokenDetails -> navigator.navigate(TokenDetails(effect.tokenId))
            }
        },
    ) { state, onIntent ->
        MainLayout(state = state, onIntent = onIntent)

        if (state.isWalletSelectorVisible) {
            WalletSelectorSheet(onDismiss = { onIntent(MainIntent.DismissWalletSelector) })
        }
    }
}
