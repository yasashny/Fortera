package com.yasashny.fortera.core.ui.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

/**
 * String that can be localised at render time.
 *
 * Used where ViewModels emit user-facing text but don't have a resources instance —
 * UI resolves it with [asString] (Composable) or [asString(context)] (non-Composable,
 * e.g. inside an `onEffect` lambda).
 *
 * ```kotlin
 * // ViewModel
 * sendEffect(FooEffect.ShowError(UiText.of(R.string.foo_save_failed)))
 *
 * // Screen (Composable)
 * ErrorDialog(message = state.errorMessage, onDismiss = { ... })
 *
 * // Effect handler — has Context access via LocalContext
 * val context = LocalContext.current
 * onEffect = { effect -> snackbar.showSnackbar(effect.message.asString(context)) }
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

/** Resolve inside Composable context — the common path for dialogs, text, labels. */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Literal -> text
    is UiText.Resource -> stringResource(id, *args.toTypedArray())
}

/** Resolve with an explicit [Context] — for snackbars / effect handlers where no Composable is in scope. */
fun UiText.asString(context: Context): String = when (this) {
    is UiText.Literal -> text
    is UiText.Resource -> context.getString(id, *args.toTypedArray())
}
