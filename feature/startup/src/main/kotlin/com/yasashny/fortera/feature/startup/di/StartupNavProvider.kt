package com.yasashny.fortera.feature.startup.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.startup.Startup
import com.yasashny.fortera.feature.startup.ui.StartupScreen

class StartupNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is Startup -> NavEntry(key) { StartupScreen() }
        else -> null
    }
}