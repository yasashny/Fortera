package com.yasashny.fortera.feature.receive.di

import androidx.navigation3.runtime.NavEntry
import com.yasashny.fortera.core.navigation.FeatureNavProvider
import com.yasashny.fortera.feature.receive.ConfirmSend
import com.yasashny.fortera.feature.receive.ReceiveToken
import com.yasashny.fortera.feature.receive.SelectTokenForReceive
import com.yasashny.fortera.feature.receive.SelectTokenForSend
import com.yasashny.fortera.feature.receive.SendSuccess
import com.yasashny.fortera.feature.receive.SendToken
import com.yasashny.fortera.feature.receive.confirmsend.ConfirmSendScreen
import com.yasashny.fortera.feature.receive.receive.ReceiveScreen
import com.yasashny.fortera.feature.receive.selecttoken.SelectTokenScreen
import com.yasashny.fortera.feature.receive.selecttokenforsend.SelectTokenForSendScreen
import com.yasashny.fortera.feature.receive.send.SendScreen
import com.yasashny.fortera.feature.receive.sendsuccess.SendSuccessScreen

internal class ReceiveNavProvider : FeatureNavProvider {
    override fun entryFor(key: Any): NavEntry<*>? = when (key) {
        is SelectTokenForReceive -> NavEntry(key) { SelectTokenScreen() }
        is ReceiveToken -> NavEntry(key) { ReceiveScreen(tokenId = key.tokenId) }
        is SelectTokenForSend -> NavEntry(key) { SelectTokenForSendScreen() }
        is SendToken -> NavEntry(key) { SendScreen(tokenId = key.tokenId) }
        is ConfirmSend -> NavEntry(key) { ConfirmSendScreen(tokenId = key.tokenId, amount = key.amount, address = key.address) }
        is SendSuccess -> NavEntry(key) {
            SendSuccessScreen(tokenId = key.tokenId, amount = key.amount, symbol = key.symbol)
        }
        else -> null
    }
}
