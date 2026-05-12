package com.yasashny.fortera.feature.managetokens.presentation

import com.yasashny.fortera.core.mvi.UiIntent

sealed interface ManageTokensIntent : UiIntent {
    data class Toggle(val tokenId: String) : ManageTokensIntent
}
