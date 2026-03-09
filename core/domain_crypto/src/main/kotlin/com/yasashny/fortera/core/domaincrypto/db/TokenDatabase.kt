package com.yasashny.fortera.core.domaincrypto.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TokenEntity::class, WalletTokenEntity::class, CachedBalanceEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class TokenDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
}
