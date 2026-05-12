package com.yasashny.fortera.feature.walletselector.main.presentation

import androidx.compose.runtime.Immutable
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

@Immutable
data class WalletSelectorState(
    val wallets: WalletsState = WalletsState.Loading,
    val errorMessage: UiText? = null,
) : UiState

@Immutable
sealed interface WalletsState {
    data object Loading : WalletsState
    data class Ready(
        val items: List<Wallet>,
        val activeWalletId: String?,
    ) : WalletsState
}
