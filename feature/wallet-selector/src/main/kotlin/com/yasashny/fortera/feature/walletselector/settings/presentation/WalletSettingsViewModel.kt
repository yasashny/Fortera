package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract.Effect
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract.Intent
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract.State

class WalletSettingsViewModel(
    private val walletId: String,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<State, Intent, Effect>(State.Loading) {

    init {
        intent {
            walletInteractor.getWallets().collect { wallets ->
                val wallet = wallets.find { it.id == walletId } ?: return@collect
                if (currentState is State.Loading) {
                    reduce(State.Content(walletId = wallet.id, name = wallet.name))
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) = when (intent) {
        is Intent.NameChanged -> updateContent { it.copy(name = intent.value) }
        Intent.SaveClicked -> saveName()
        Intent.DeleteClicked -> updateContent { it.copy(showDeleteDialog = true) }
        Intent.DismissDeleteDialog -> updateContent { it.copy(showDeleteDialog = false) }
        Intent.ConfirmDelete -> deleteWallet()
        Intent.BackClicked -> sendEffect(Effect.NavigateBack)
        Intent.DismissError -> updateContent { it.copy(errorMessage = null) }
    }

    private fun saveName() {
        intent {
            withState<State.Content> { content ->
                if (content.name.isBlank()) {
                    reduce(content.copy(errorMessage = UiText.of(R.string.wallet_settings_error_name_empty)))
                    return@withState
                }
                reduce(content.copy(isSaving = true, errorMessage = null))
                walletInteractor.updateWalletName(content.walletId, content.name)
                    .onSuccess {
                        reduce(content.copy(isSaving = false))
                        sendEffect(Effect.ShowSuccess(UiText.of(R.string.wallet_settings_name_saved)))
                    }
                    .onFailure {
                        reduce(content.copy(isSaving = false, errorMessage = UiText.of(R.string.wallet_settings_error_save_failed)))
                    }
            }
        }
    }

    private fun deleteWallet() {
        intent {
            withState<State.Content> { content ->
                reduce(content.copy(showDeleteDialog = false, isSaving = true, errorMessage = null))
                walletInteractor.deleteWallet(content.walletId)
                    .onSuccess { sendEffect(Effect.NavigateBack) }
                    .onFailure {
                        reduce(content.copy(isSaving = false, errorMessage = UiText.of(R.string.wallet_settings_error_delete_failed)))
                    }
            }
        }
    }

    private inline fun updateContent(crossinline transform: (State.Content) -> State.Content) {
        updateState { state -> if (state is State.Content) transform(state) else state }
    }
}
