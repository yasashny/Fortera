package com.yasashny.fortera.core.domain.wallet.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val createdAt: Long,
)
