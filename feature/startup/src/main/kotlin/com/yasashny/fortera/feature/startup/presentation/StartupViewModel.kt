package com.yasashny.fortera.feature.startup.presentation

import com.yasashny.fortera.core.mvi.MviViewModel

class StartupViewModel : MviViewModel<StartupState, StartupIntent, StartupEffect>(StartupState) {

    override fun handleIntent(intent: StartupIntent) {
        when (intent) {
            StartupIntent.CreateWalletClicked -> sendEffect(StartupEffect.NavigateToCreateWallet)
            StartupIntent.ImportWalletClicked -> sendEffect(StartupEffect.NavigateToImportWallet)
        }
    }
}
