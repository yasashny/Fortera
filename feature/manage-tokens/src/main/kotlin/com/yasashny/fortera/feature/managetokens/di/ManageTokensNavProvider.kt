package com.yasashny.fortera.feature.managetokens.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.managetokens.AddCustomToken
import com.yasashny.fortera.feature.managetokens.ManageTokens
import com.yasashny.fortera.feature.managetokens.addtoken.ui.AddCustomTokenScreen
import com.yasashny.fortera.feature.managetokens.ui.ManageTokensScreen

class ManageTokensNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is ManageTokens -> NavEntry(key) { ManageTokensScreen() }
        is AddCustomToken -> NavEntry(key) { AddCustomTokenScreen() }
        else -> null
    }
}
