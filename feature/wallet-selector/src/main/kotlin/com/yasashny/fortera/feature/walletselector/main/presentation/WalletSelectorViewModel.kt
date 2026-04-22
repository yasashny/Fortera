package com.yasashny.fortera.feature.walletselector.main.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R
import kotlinx.coroutines.flow.combine

class WalletSelectorViewModel(
    private val walletInteractor: WalletInteractor,
) : MviViewModel<WalletSelectorContract.State, WalletSelectorContract.Intent, WalletSelectorContract.Effect>(
    WalletSelectorContract.State(),
) {

    init {
        intent {
            launch {
                combine(
                    walletInteractor.getWallets(),
                    walletInteractor.observeActiveWallet(),
                ) { wallets, activeWallet ->
                    currentState.copy(
                        wallets = wallets,
                        activeWalletId = activeWallet?.id,
                        isLoading = false,
                    )
                }.collect { reduce(it) }
            }
        }
    }

    override fun handleIntent(intent: WalletSelectorContract.Intent) {
        when (intent) {
            is WalletSelectorContract.Intent.SelectWallet -> selectWallet(intent.id)
            is WalletSelectorContract.Intent.SettingsClicked ->
                sendEffect(WalletSelectorContract.Effect.NavigateToSettings(intent.walletId))
            WalletSelectorContract.Intent.CreateWalletClicked ->
                sendEffect(WalletSelectorContract.Effect.NavigateToCreateWallet)
            WalletSelectorContract.Intent.ImportWalletClicked ->
                sendEffect(WalletSelectorContract.Effect.NavigateToImportWallet)
            WalletSelectorContract.Intent.DismissError ->
                updateState { it.copy(errorMessage = null) }
        }
    }

    private fun selectWallet(id: String) = intent {
        walletInteractor.setActiveWallet(id)
            .onFailure { throwable ->
                val message = throwable.message?.takeIf { it.isNotBlank() }?.let(UiText::of)
                    ?: UiText.of(R.string.wallet_selector_error_select_failed)
                updateState { it.copy(errorMessage = message) }
            }
    }
}
