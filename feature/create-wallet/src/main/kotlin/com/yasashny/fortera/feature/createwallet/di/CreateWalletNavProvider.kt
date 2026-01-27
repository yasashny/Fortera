package com.yasashny.fortera.feature.createwallet.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.createwallet.CreateWallet
import com.yasashny.fortera.feature.createwallet.ui.CreateWalletScreen

class CreateWalletNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is CreateWallet -> NavEntry(key) { CreateWalletScreen() }
        else -> null
    }
}