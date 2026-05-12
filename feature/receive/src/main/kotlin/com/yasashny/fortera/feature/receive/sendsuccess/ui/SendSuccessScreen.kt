package com.yasashny.fortera.feature.receive.sendsuccess.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.main.Main
import com.yasashny.fortera.feature.tokendetails.TokenDetails

@Composable
internal fun SendSuccessScreen(
    tokenId: String,
    amount: String,
    symbol: String,
) {
    val navigator = LocalAppNavigator.current
    val close = { navigator.clearAndNavigate(Main, TokenDetails(tokenId)) }

    BackHandler(onBack = close)

    SendSuccessLayout(
        amount = amount,
        symbol = symbol,
        onCloseClick = close,
    )
}
