package com.yasashny.fortera.feature.tokendetails.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.tokendetails.TokenDetails
import com.yasashny.fortera.feature.tokendetails.ui.TokenDetailsScreen

class TokenDetailsNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is TokenDetails -> NavEntry(key) { TokenDetailsScreen(tokenId = key.tokenId) }
        else -> null
    }
}
