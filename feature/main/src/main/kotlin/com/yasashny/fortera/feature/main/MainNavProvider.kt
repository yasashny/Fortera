package com.yasashny.fortera.feature.main

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider

class MainNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is Main -> NavEntry(key) { MainScreen() }
        else -> null
    }
}
