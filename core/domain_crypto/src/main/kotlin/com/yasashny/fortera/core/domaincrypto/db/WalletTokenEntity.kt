package com.yasashny.fortera.core.domaincrypto.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "wallet_tokens",
    primaryKeys = ["walletId", "tokenId"],
    foreignKeys = [
        ForeignKey(
            entity = TokenEntity::class,
            parentColumns = ["id"],
            childColumns = ["tokenId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tokenId")],
)
data class WalletTokenEntity(
    val walletId: String,
    @ColumnInfo(index = false) val tokenId: String,
)
