package com.yasashny.fortera.feature.walletselector.settings.presentation

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel

class WalletSettingsViewModel(
    private val walletId: String,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<WalletSettingsContract.State, WalletSettingsContract.Intent, WalletSettingsContract.Effect>(
    WalletSettingsContract.State.Loading
) {

    init {
        intent {
            launch {
                walletInteractor.getWallets().collect { wallets ->
                    val wallet = wallets.find { it.id == walletId } ?: return@collect
                    withState<WalletSettingsContract.State.Loading> {
                        reduce(
                            WalletSettingsContract.State.Content(
                                walletId = walletId, name = wallet.name
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
            WalletSettingsContract.Intent.BackClicked -> intent { sendEffect(WalletSettingsContract.Effect.NavigateBack) }
        }
    }

    private fun onNameChanged(value: String) {
        updateState { state ->
            (state as? WalletSettingsContract.State.Content)?.copy(name = value) ?: state
        }
    }

    private fun onSaveClicked() = intent {
        withState<WalletSettingsContract.State.Content> { content ->
            if (content.name.isBlank()) {
                sendEffect(WalletSettingsContract.Effect.ShowError("Wallet name cannot be empty"))
                return@withState
            }
            reduce(content.copy(isSaving = true))
            walletInteractor.updateWalletName(content.walletId, content.name)
                .onSuccess {
                    reduce(content.copy(isSaving = false))
                    sendEffect(WalletSettingsContract.Effect.ShowSuccess("Wallet name saved"))
                }.onFailure {
                    reduce(content.copy(isSaving = false))
                    sendEffect(
                        WalletSettingsContract.Effect.ShowError(
                            it.message ?: "Failed to save"
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
            (state as? WalletSettingsContract.State.Content)?.copy(showDeleteDialog = false)
                ?: state
        }
    }

    private fun onConfirmDelete() = intent {
        withState<WalletSettingsContract.State.Content> { content ->
            reduce(content.copy(showDeleteDialog = false, isSaving = true))
            walletInteractor.deleteWallet(content.walletId)
                .onSuccess { sendEffect(WalletSettingsContract.Effect.NavigateBack) }.onFailure {
                    reduce(content.copy(isSaving = false))
                    sendEffect(
                        WalletSettingsContract.Effect.ShowError(
                            it.message ?: "Failed to delete wallet"
                        )
                    )
                }
        }
    }
}