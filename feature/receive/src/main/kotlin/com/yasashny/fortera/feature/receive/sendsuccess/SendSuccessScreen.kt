package com.yasashny.fortera.feature.receive.sendsuccess

import androidx.compose.runtime.Composable
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.main.Main

@Composable
internal fun SendSuccessScreen() {
    val navigator = LocalAppNavigator.current

    SendSuccessLayout(
        onCloseClick = { navigator.clearAndNavigate(Main) },
    )
}
