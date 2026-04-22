package com.yasashny.fortera.core.datacrypto.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_balances",
    primaryKeys = ["walletId", "tokenId"],
    indices = [Index("walletId")],
)
data class CachedBalanceEntity(
    val walletId: String,
    val tokenId: String,
    val balance: String,
    val priceUsd: Double,
    val changePercent24h: Double,
)
