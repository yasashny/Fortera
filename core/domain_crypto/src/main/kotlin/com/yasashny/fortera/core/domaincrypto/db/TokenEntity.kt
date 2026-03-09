package com.yasashny.fortera.core.domaincrypto.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val network: String,
    val contractAddress: String?,
    val coingeckoId: String,
    val isDefault: Boolean,
)

fun TokenEntity.toTokenDefinition() = TokenDefinition(
    id = id,
    name = name,
    symbol = symbol,
    network = BlockchainNetwork.valueOf(network),
    contractAddress = contractAddress,
    coingeckoId = coingeckoId,
    isDefault = isDefault,
)

fun TokenDefinition.toEntity() = TokenEntity(
    id = id,
    name = name,
    symbol = symbol,
    network = network.name,
    contractAddress = contractAddress,
    coingeckoId = coingeckoId,
    isDefault = isDefault,
)
