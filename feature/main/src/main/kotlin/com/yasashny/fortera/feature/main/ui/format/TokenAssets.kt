package com.yasashny.fortera.feature.main.ui.format

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition

private const val ICON_BASE =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color"

internal fun iconUrl(symbol: String): String = "$ICON_BASE/${symbol.lowercase()}.png"

internal fun networkBadgeUrlFor(token: TokenDefinition): String? =
    if (token.contractAddress != null) iconUrl("eth") else null
