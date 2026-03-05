package com.yasashny.fortera.feature.receive.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.receive.ReceiveToken
import com.yasashny.fortera.feature.receive.SelectTokenForReceive
import com.yasashny.fortera.feature.receive.receive.ReceiveScreen
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenScreen

internal class ReceiveNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is SelectTokenForReceive -> NavEntry(key) { SelectTokenScreen() }
        is ReceiveToken -> NavEntry(key) { ReceiveScreen(tokenId = key.tokenId) }
        else -> null
    }
}
