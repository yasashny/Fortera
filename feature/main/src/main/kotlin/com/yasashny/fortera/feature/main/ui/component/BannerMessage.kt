package com.yasashny.fortera.feature.main.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yasashny.fortera.feature.main.R
import com.yasashny.fortera.feature.main.presentation.Banner

@Composable
internal fun Banner.asDisplayText(): String = when (this) {
    Banner.GenericError -> stringResource(R.string.main_load_error)
    is Banner.NetworksUnavailable -> {
        val list = networks.joinToString(", ")
        if (networks.size == 1) {
            stringResource(R.string.main_network_unavailable_single, list)
        } else {
            stringResource(R.string.main_networks_unavailable_multiple, list)
        }
    }
}
