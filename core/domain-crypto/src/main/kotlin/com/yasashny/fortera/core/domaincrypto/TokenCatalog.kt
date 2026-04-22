package com.yasashny.fortera.core.domaincrypto

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition

object TokenCatalog {

    val tokens: List<TokenDefinition> = listOf(
        TokenDefinition(
            id = "bitcoin",
            name = "Bitcoin",
            symbol = "BTC",
            network = BlockchainNetwork.BITCOIN,
            decimals = 8,
            contractAddress = null,
            coingeckoId = "bitcoin",
            isDefault = true,
        ),
        TokenDefinition(
            id = "ethereum",
            name = "Ethereum",
            symbol = "ETH",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 18,
            contractAddress = null,
            coingeckoId = "ethereum",
            isDefault = true,
        ),
        TokenDefinition(
            id = "tether",
            name = "Tether",
            symbol = "USDT",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 6,
            contractAddress = "0xdAC17F958D2ee523a2206206994597C13D831ec7",
            coingeckoId = "tether",
            isDefault = true,
        ),
        TokenDefinition(
            id = "usd-coin",
            name = "USD Coin",
            symbol = "USDC",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 6,
            contractAddress = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
            coingeckoId = "usd-coin",
            isDefault = true,
        ),
        TokenDefinition(
            id = "dai",
            name = "Dai",
            symbol = "DAI",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 18,
            contractAddress = "0x6B175474E89094C44Da98b954EedeAC495271d0F",
            coingeckoId = "dai",
            isDefault = false,
        ),
        TokenDefinition(
            id = "chainlink",
            name = "Chainlink",
            symbol = "LINK",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 18,
            contractAddress = "0x514910771AF9Ca656af840dff83E8264EcF986CA",
            coingeckoId = "chainlink",
            isDefault = false,
        ),
        TokenDefinition(
            id = "uniswap",
            name = "Uniswap",
            symbol = "UNI",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 18,
            contractAddress = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984",
            coingeckoId = "uniswap",
            isDefault = false,
        ),
        TokenDefinition(
            id = "aave",
            name = "Aave",
            symbol = "AAVE",
            network = BlockchainNetwork.ETHEREUM,
            decimals = 18,
            contractAddress = "0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9",
            coingeckoId = "aave",
            isDefault = false,
        ),
    )

    val defaultTokenIds: Set<String> = tokens.filter { it.isDefault }.map { it.id }.toSet()
}
