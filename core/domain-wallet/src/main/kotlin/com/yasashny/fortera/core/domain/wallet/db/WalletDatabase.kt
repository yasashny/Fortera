package com.yasashny.fortera.core.domain.wallet.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WalletEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
}
