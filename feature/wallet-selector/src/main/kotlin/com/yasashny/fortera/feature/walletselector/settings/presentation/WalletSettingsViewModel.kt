package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.walletselector.R

class WalletSettingsViewModel(
    private val walletId: String,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<WalletSettingsContract.State, WalletSettingsContract.Intent, WalletSettingsContract.Effect>(
    WalletSettingsContract.State.Loading,
) {

    init {
        intent {
            launch {
                walletInteractor.getWallets().collect { wallets ->
                    val wallet = wallets.find { it.id == walletId } ?: return@collect
                    withState<WalletSettingsContract.State.Loading> {
                        reduce(
                            WalletSettingsContract.State.Content(
                                walletId = walletId, name = wallet.name,
                            )
                        )
                    }
                }
            }
        }
    }

    override fun handleIntent(intent: WalletSettingsContract.Intent) {
        when (intent) {
            is WalletSettingsContract.Intent.NameChanged -> onNameChanged(intent.value)
            WalletSettingsContract.Intent.SaveClicked -> onSaveClicked()
            WalletSettingsContract.Intent.DeleteClicked -> onDeleteClicked()
            WalletSettingsContract.Intent.ConfirmDelete -> onConfirmDelete()
            WalletSettingsContract.Intent.DismissDeleteDialog -> onDismissDeleteDialog()
            WalletSettingsContract.Intent.BackClicked -> sendEffect(WalletSettingsContract.Effect.NavigateBack)
            WalletSettingsContract.Intent.DismissError -> clearError()
        }
    }

    private fun onNameChanged(value: String) {
        updateState { state ->
            (state as? WalletSettingsContract.State.Content)?.copy(name = value) ?: state
        }
    }

    private fun clearError() {
        updateState { state ->
            (state as? WalletSettingsContract.State.Content)?.copy(errorMessage = null) ?: state
        }
    }

    private fun onSaveClicked() = intent {
        withState<WalletSettingsContract.State.Content> { content ->
            if (content.name.isBlank()) {
                reduce(content.copy(errorMessage = UiText.of(R.string.wallet_settings_error_name_empty)))
                return@withState
            }
            reduce(content.copy(isSaving = true, errorMessage = null))
            walletInteractor.updateWalletName(content.walletId, content.name)
                .onSuccess {
                    reduce(content.copy(isSaving = false))
                    sendEffect(
                        WalletSettingsContract.Effect.ShowSuccess(
                            UiText.of(R.string.wallet_settings_name_saved)
                        )
                    )
                }
                .onFailure { throwable ->
                    reduce(
                        content.copy(
                            isSaving = false,
                            errorMessage = mapError(throwable, R.string.wallet_settings_error_save_failed),
                        )
                    )
                }
        }
    }

    private fun onDeleteClicked() {
        updateState { state ->
            (state as? WalletSettingsContract.State.Content)?.copy(showDeleteDialog = true) ?: state
        }
    }

    private fun onDismissDeleteDialog() {
        updateState { state ->
            (state as? WalletSettingsContract.State.Content)?.copy(showDeleteDialog = false) ?: state
        }
    }

    private fun onConfirmDelete() = intent {
        withState<WalletSettingsContract.State.Content> { content ->
            reduce(content.copy(showDeleteDialog = false, isSaving = true, errorMessage = null))
            walletInteractor.deleteWallet(content.walletId)
                .onSuccess { sendEffect(WalletSettingsContract.Effect.NavigateBack) }
                .onFailure { throwable ->
                    reduce(
                        content.copy(
                            isSaving = false,
                            errorMessage = mapError(throwable, R.string.wallet_settings_error_delete_failed),
                        )
                    )
                }
        }
    }

    private fun mapError(throwable: Throwable, fallback: Int): UiText =
        throwable.message?.takeIf { it.isNotBlank() }?.let(UiText::of) ?: UiText.of(fallback)
}
