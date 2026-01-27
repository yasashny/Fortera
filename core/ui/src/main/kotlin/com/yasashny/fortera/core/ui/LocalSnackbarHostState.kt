package com.yasashny.fortera.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarHostState not provided")
}
