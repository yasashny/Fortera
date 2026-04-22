package com.yasashny.fortera.core.ui.token

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition

private const val ICON_BASE =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color"

/** URL of the colour icon for a given symbol (e.g. "BTC" -> ".../btc.png"). */
fun tokenIconUrl(symbol: String): String = "$ICON_BASE/${symbol.lowercase()}.png"

/** Small badge overlay on a token icon — ETH glyph for ERC-20 tokens, null otherwise. */
fun networkBadgeUrlFor(token: TokenDefinition): String? =
    if (token.contractAddress != null) tokenIconUrl("eth") else null
