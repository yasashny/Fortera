package com.yasashny.fortera.core.datacrypto.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {

    @Query("SELECT * FROM tokens")
    suspend fun getAllTokens(): List<TokenEntity>

    @Query("SELECT * FROM tokens")
    fun observeAllTokens(): Flow<List<TokenEntity>>

    @Query("SELECT * FROM tokens WHERE id = :id")
    suspend fun getTokenById(id: String): TokenEntity?

    @Query("SELECT * FROM tokens WHERE contractAddress IS NOT NULL AND LOWER(contractAddress) = LOWER(:contractAddress) LIMIT 1")
    suspend fun getTokenByContract(contractAddress: String): TokenEntity?

    @Query("SELECT COUNT(*) FROM tokens")
    suspend fun getTokenCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: List<TokenEntity>)

    @Query("SELECT id FROM tokens WHERE isDefault = 1")
    suspend fun getDefaultTokenIds(): List<String>

    @Query("SELECT tokenId FROM wallet_tokens WHERE walletId = :walletId")
    fun observeEnabledTokenIds(walletId: String): Flow<List<String>>

    @Query("SELECT tokenId FROM wallet_tokens WHERE walletId = :walletId")
    suspend fun getEnabledTokenIds(walletId: String): List<String>

    @Query("SELECT COUNT(*) FROM wallet_tokens WHERE walletId = :walletId")
    suspend fun countWalletTokens(walletId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTokens(walletTokens: List<WalletTokenEntity>)

    @Query("DELETE FROM wallet_tokens WHERE walletId = :walletId AND tokenId = :tokenId")
    suspend fun deleteWalletToken(walletId: String, tokenId: String)

    @Query("SELECT * FROM cached_balances WHERE walletId = :walletId AND tokenId IN (:tokenIds)")
    suspend fun getCachedBalances(walletId: String, tokenIds: List<String>): List<CachedBalanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedBalances(balances: List<CachedBalanceEntity>)

    @Transaction
    suspend fun replaceCachedBalances(walletId: String, balances: List<CachedBalanceEntity>) {
        deleteCachedBalancesForWallet(walletId)
        insertCachedBalances(balances)
    }

    @Query("DELETE FROM cached_balances WHERE walletId = :walletId")
    suspend fun deleteCachedBalancesForWallet(walletId: String)

    @Query("DELETE FROM cached_balances")
    suspend fun clearAllCachedBalances()
}
