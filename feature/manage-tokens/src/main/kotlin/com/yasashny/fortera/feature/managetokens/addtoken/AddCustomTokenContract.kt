package com.yasashny.fortera.feature.managetokens.addtoken

import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

object AddCustomTokenContract {

    /**
     * Result of asking [com.yasashny.fortera.core.domaincrypto.repository.TokenMetadataFetcher]
     * about the current input. The state machine is intentionally explicit so the UI can
     * render the right thing per phase (idle hint, spinner, preview, or error).
     */
    sealed interface Verification {
        /** Initial state, also after the user clears the input. */
        data object Idle : Verification

        /** A network call is in flight for the current input value. */
        data object InProgress : Verification

        /**
         * Lookup succeeded. [alreadyAdded] is true when the token is already in the
         * user's catalog — we still let them confirm to re-enable it on the active wallet.
         */
        data class Verified(
            val metadata: CustomTokenMetadata,
            val alreadyAdded: Boolean,
        ) : Verification

        /** Lookup completed but rejected the input. */
        data class Failed(val message: UiText) : Verification
    }

    data class State(
        val input: String = "",
        val verification: Verification = Verification.Idle,
        val isAdding: Boolean = false,
    ) : UiState {
        val canAdd: Boolean
            get() = verification is Verification.Verified && !isAdding
    }

    sealed interface Intent : UiIntent {
        data class InputChanged(val value: String) : Intent
        data object AddClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowError(val message: UiText) : Effect
    }
}
