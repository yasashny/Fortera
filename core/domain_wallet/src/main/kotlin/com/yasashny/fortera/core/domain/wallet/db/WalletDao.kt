package com.yasashny.fortera.core.domain.wallet.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets ORDER BY createdAt DESC")
    fun getAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WalletEntity?

    @Upsert
    suspend fun upsert(wallet: WalletEntity)

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getCount(): Int

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteById(id: String)
}
