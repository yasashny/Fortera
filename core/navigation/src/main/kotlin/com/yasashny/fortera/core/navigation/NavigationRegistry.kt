package com.yasashny.fortera.core.navigation

import androidx.navigation3.runtime.NavEntry

class NavigationRegistry(private val providers: List<FeatureNavProvider>) {
    fun entryFor(key: Any): NavEntry<*>? =
        providers.firstNotNullOfOrNull { it.entryFor(key) }
}
