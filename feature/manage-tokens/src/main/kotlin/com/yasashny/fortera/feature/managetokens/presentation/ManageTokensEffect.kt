package com.yasashny.fortera.feature.managetokens.presentation

import com.yasashny.fortera.core.mvi.UiEffect

sealed interface ManageTokensEffect : UiEffect {
    data object NavigateBack : ManageTokensEffect
}
