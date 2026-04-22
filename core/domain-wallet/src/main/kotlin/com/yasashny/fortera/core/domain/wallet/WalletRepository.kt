package com.yasashny.fortera.core.domain.wallet

import kotlinx.coroutines.flow.Flow

interface WalletRepository {

    fun getWallets(): Flow<List<Wallet>>

    fun observeActiveWallet(): Flow<Wallet?>

    suspend fun setActiveWallet(walletId: String)

    suspend fun createWallet(name: String, seedPhrase: SeedPhrase): Wallet

    suspend fun importWallet(name: String, seedPhrase: SeedPhrase): Wallet

    suspend fun updateWalletName(id: String, name: String)

    suspend fun deleteWallet(id: String)

    suspend fun getWalletCount(): Int

    suspend fun getSeedPhrase(walletId: String): SeedPhrase?
}
