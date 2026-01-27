package com.yasashny.fortera.core.navigation

import androidx.compose.runtime.compositionLocalOf

val LocalAppNavigator = compositionLocalOf<AppNavigator> {
    error("LocalAppNavigator not provided")
}
