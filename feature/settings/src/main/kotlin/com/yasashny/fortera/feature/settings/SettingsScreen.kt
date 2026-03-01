package com.yasashny.fortera.feature.settings

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.navigation.LocalAppNavigator

@Composable
internal fun SettingsScreen() {
    val navigator = LocalAppNavigator.current

    SettingsLayout(onBack = { navigator.back() })
}
