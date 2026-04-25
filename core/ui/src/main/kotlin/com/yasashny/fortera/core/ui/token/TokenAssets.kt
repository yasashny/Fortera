package com.yasashny.fortera.core.ui.token

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition

private const val SPOTHQ_BASE =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color"

private const val ONEINCH_BASE = "https://tokens-data.1inch.io/images"

private const val ONEINCH_NATIVE_ETH = "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"

fun tokenIconUrl(symbol: String): String = "$SPOTHQ_BASE/${symbol.lowercase()}.png"

fun tokenIconUrl(token: TokenDefinition): String {
    val contract = token.contractAddress
    return when {
        contract != null -> oneInchIcon(contract)
        token.network == BlockchainNetwork.ETHEREUM -> oneInchIcon(ONEINCH_NATIVE_ETH)
        else -> tokenIconUrl(token.symbol)
    }
}

fun tokenIconUrl(metadata: CustomTokenMetadata): String = oneInchIcon(metadata.contractAddress)

fun networkBadgeUrlFor(token: TokenDefinition): String? =
    if (token.contractAddress != null) tokenIconUrl("eth") else null

private fun oneInchIcon(contractAddress: String): String =
    "$ONEINCH_BASE/${contractAddress.lowercase()}.png"
