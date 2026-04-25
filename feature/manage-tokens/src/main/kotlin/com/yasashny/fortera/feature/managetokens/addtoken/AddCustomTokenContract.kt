package com.yasashny.fortera.feature.managetokens.addtoken

import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

object AddCustomTokenContract {

    sealed interface Verification {
        data object Idle : Verification

        data object InProgress : Verification

        data class Verified(
            val metadata: CustomTokenMetadata,
            val alreadyAdded: Boolean,
        ) : Verification

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
