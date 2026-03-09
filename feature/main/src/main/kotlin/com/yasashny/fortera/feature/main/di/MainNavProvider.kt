package com.yasashny.fortera.feature.main.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.main.Main
import com.yasashny.fortera.feature.main.ui.MainScreen

class MainNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is Main -> NavEntry(key) { MainScreen() }
        else -> null
    }
}