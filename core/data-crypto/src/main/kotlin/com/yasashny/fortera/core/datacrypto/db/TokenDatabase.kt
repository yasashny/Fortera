package com.yasashny.fortera.core.datacrypto.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TokenEntity::class, WalletTokenEntity::class, CachedBalanceEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class TokenDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
}
