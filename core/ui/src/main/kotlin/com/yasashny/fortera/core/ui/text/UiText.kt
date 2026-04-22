package com.yasashny.fortera.core.ui.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

/**
 * String that can be localised at render time.
 *
 * Used where ViewModels emit user-facing text but don't have a resources instance —
 * the Composable resolves [asString] via [stringResource].
 *
 * ```kotlin
 * // ViewModel
 * sendEffect(FooEffect.ShowError(UiText.of(R.string.foo_save_failed)))
 *
 * // Screen
 * is FooEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.asString())
 * ```
 */
@Immutable
sealed interface UiText {
    data class Literal(val text: String) : UiText
    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    companion object {
        fun of(@StringRes id: Int, vararg args: Any): UiText =
            Resource(id, args.toList())

        fun of(literal: String): UiText = Literal(literal)
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Literal -> text
    is UiText.Resource -> stringResource(id, *args.toTypedArray())
}
