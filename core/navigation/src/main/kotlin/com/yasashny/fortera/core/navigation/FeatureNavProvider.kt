package com.yasashny.fortera.core.navigation

import androidx.navigation3.runtime.NavEntry

interface FeatureNavProvider {
    fun entryFor(key: Any): NavEntry<*>?
}
