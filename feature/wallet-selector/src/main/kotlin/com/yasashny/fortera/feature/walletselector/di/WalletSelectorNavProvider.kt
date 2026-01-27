package com.yasashny.fortera.feature.walletselector.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.walletselector.WalletSettings
import com.yasashny.fortera.feature.walletselector.settings.ui.WalletSettingsScreen

class WalletSelectorNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is WalletSettings -> NavEntry(key) { WalletSettingsScreen(walletId = key.walletId) }
        else -> null
    }
}