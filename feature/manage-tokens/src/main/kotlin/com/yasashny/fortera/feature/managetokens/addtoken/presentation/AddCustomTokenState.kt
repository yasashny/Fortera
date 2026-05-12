package com.yasashny.fortera.feature.managetokens.addtoken.presentation

import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText

data class AddCustomTokenState(
    val input: String = "",
    val verification: Verification = Verification.Idle,
    val isAdding: Boolean = false,
) : UiState {
    val canAdd: Boolean
        get() = verification is Verification.Verified && !isAdding
}

sealed interface Verification {
    data object Idle : Verification

    data object InProgress : Verification

    data class Verified(
        val metadata: CustomTokenMetadata,
        val alreadyAdded: Boolean,
    ) : Verification

    data class Failed(val message: UiText) : Verification
}
