package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R

class WalletSettingsViewModel(
    private val walletId: String,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<WalletSettingsState, WalletSettingsIntent, WalletSettingsEffect>(WalletSettingsState.Loading) {

    init {
        intent {
            walletInteractor.getWallets().collect { wallets ->
                val wallet = wallets.find { it.id == walletId } ?: return@collect
                if (currentState is WalletSettingsState.Loading) {
                    reduce(WalletSettingsState.Content(walletId = wallet.id, name = wallet.name))
                }
            }
        }
    }

    override fun handleIntent(intent: WalletSettingsIntent) = when (intent) {
        is WalletSettingsIntent.NameChanged -> updateContent { it.copy(name = intent.value) }
        WalletSettingsIntent.SaveClicked -> saveName()
        WalletSettingsIntent.DeleteClicked -> updateContent { it.copy(showDeleteDialog = true) }
        WalletSettingsIntent.DismissDeleteDialog -> updateContent { it.copy(showDeleteDialog = false) }
        WalletSettingsIntent.ConfirmDelete -> deleteWallet()
        WalletSettingsIntent.BackClicked -> sendEffect(WalletSettingsEffect.NavigateBack)
        WalletSettingsIntent.DismissError -> updateContent { it.copy(errorMessage = null) }
    }

    private fun saveName() {
        intent {
            withState<WalletSettingsState.Content> { content ->
                if (content.name.isBlank()) {
                    reduce(content.copy(errorMessage = UiText.of(R.string.wallet_settings_error_name_empty)))
                    return@withState
                }
                reduce(content.copy(isSaving = true, errorMessage = null))
                walletInteractor.updateWalletName(content.walletId, content.name)
                    .onSuccess {
                        reduce(content.copy(isSaving = false))
                        sendEffect(WalletSettingsEffect.ShowSuccess(UiText.of(R.string.wallet_settings_name_saved)))
                    }
                    .onFailure {
                        reduce(content.copy(isSaving = false, errorMessage = UiText.of(R.string.wallet_settings_error_save_failed)))
                    }
            }
        }
    }

    private fun deleteWallet() {
        intent {
            withState<WalletSettingsState.Content> { content ->
                reduce(content.copy(showDeleteDialog = false, isSaving = true, errorMessage = null))
                walletInteractor.deleteWallet(content.walletId)
                    .onSuccess { sendEffect(WalletSettingsEffect.NavigateBack) }
                    .onFailure {
                        reduce(content.copy(isSaving = false, errorMessage = UiText.of(R.string.wallet_settings_error_delete_failed)))
                    }
            }
        }
    }

    private inline fun updateContent(crossinline transform: (WalletSettingsState.Content) -> WalletSettingsState.Content) {
        updateState { state -> if (state is WalletSettingsState.Content) transform(state) else state }
    }
}
