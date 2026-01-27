package com.yasashny.fortera.feature.importwallet.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.importwallet.ImportWallet
import com.yasashny.fortera.feature.importwallet.ui.ImportWalletScreen

class ImportWalletNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is ImportWallet -> NavEntry(key) { ImportWalletScreen() }
        else -> null
    }
}