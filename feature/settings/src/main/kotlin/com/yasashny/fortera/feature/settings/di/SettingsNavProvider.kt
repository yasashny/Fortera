package com.yasashny.fortera.feature.settings.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.settings.Settings
import com.yasashny.fortera.feature.settings.SettingsScreen

class SettingsNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is Settings -> NavEntry(key) { SettingsScreen() }
        else -> null
    }
}
