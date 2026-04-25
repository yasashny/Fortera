package com.yasashny.fortera.core.ui.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

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

fun UiText.asString(context: Context): String = when (this) {
    is UiText.Literal -> text
    is UiText.Resource -> context.getString(id, *args.toTypedArray())
}
