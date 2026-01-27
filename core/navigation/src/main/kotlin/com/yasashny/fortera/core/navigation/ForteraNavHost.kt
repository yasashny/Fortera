package com.yasashny.fortera.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

@Composable
fun ForteraNavHost(
    navigator: AppNavigator,
    registry: NavigationRegistry,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalAppNavigator provides navigator) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::back,
            modifier = modifier.fillMaxSize(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                @Suppress("UNCHECKED_CAST")
                registry.entryFor(key) as? NavEntry<Any> ?: NavEntry(Unit) { }
            }
        )
    }
}
