package com.yasashny.fortera.feature.main.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.feature.main.domain.MainOverviewEvent
import com.yasashny.fortera.feature.main.domain.MainOverviewInteractor
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

class MainViewModel(
    private val overviewInteractor: MainOverviewInteractor,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<MainState, MainIntent, MainEffect>(MainState.Initial) {

    init {
        observeOverview()
        observeWalletName()
    }

    override fun handleIntent(intent: MainIntent) {
        when (intent) {
            MainIntent.Refresh -> refresh()
            MainIntent.OpenWalletSelector -> updateState { it.copy(isWalletSelectorVisible = true) }
            MainIntent.DismissWalletSelector -> updateState { it.copy(isWalletSelectorVisible = false) }
            MainIntent.DismissBanner -> updateState { it.copy(banner = null) }
            MainIntent.OpenSettings -> sendEffect(MainEffect.NavigateToSettings)
            MainIntent.OpenSend -> sendEffect(MainEffect.NavigateToSend)
            MainIntent.OpenReceive -> sendEffect(MainEffect.NavigateToReceive)
            MainIntent.OpenManageTokens -> sendEffect(MainEffect.NavigateToManageTokens)
            is MainIntent.OpenTokenDetails -> sendEffect(MainEffect.NavigateToTokenDetails(intent.tokenId))
        }
    }

    private fun observeOverview() = intent {
        launch {
            overviewInteractor.observe().collect { event ->
                when (event) {
                    MainOverviewEvent.NoWallets ->
                        sendEffect(MainEffect.NavigateToStartup)

                    is MainOverviewEvent.WalletActivated ->
                        updateState { MainReducer.onWalletActivated(it, event.walletName) }

                    is MainOverviewEvent.Data ->
                        updateState { MainReducer.onOverview(it, event) }

                    is MainOverviewEvent.Failed ->
                        updateState { MainReducer.onLoadFailed(it) }
                }
            }
        }
    }

    private fun observeWalletName() = intent {
        launch {
            walletInteractor.observeActiveWallet()
                .mapNotNull { it?.name }
                .distinctUntilChanged()
                .collect { name ->
                    updateState { MainReducer.onWalletNameChanged(it, name) }
                }
        }
    }

    private fun refresh() = intent {
        updateState { MainReducer.onRefreshStarted(it) }
        overviewInteractor.refresh()
            .onSuccess { overview ->
                updateState { MainReducer.onRefreshSucceeded(it, overview) }
            }
            .onFailure {
                updateState { MainReducer.onRefreshFailed(it) }
            }
    }
}
