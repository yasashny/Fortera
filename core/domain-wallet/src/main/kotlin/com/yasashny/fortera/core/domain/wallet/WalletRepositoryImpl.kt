package com.yasashny.fortera.core.domain.wallet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yasashny.fortera.core.database.secure.SecureStorage
import com.yasashny.fortera.core.domain.wallet.db.WalletDao
import com.yasashny.fortera.core.domain.wallet.db.WalletEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

internal class WalletRepositoryImpl(
    private val walletDao: WalletDao,
    private val dataStore: DataStore<Preferences>,
    private val secureStorage: SecureStorage,
) : WalletRepository {

    override fun getWallets(): Flow<List<Wallet>> =
        walletDao.getAll().map { it.map(WalletEntity::toWallet) }

    override fun observeActiveWallet(): Flow<Wallet?> =
        combine(
            walletDao.getAll().map { it.map(WalletEntity::toWallet) },
            dataStore.data.map { it[ACTIVE_WALLET_ID_KEY] },
        ) { wallets, activeId ->
            activeId?.let { id -> wallets.find { it.id == id } } ?: wallets.firstOrNull()
        }

    override suspend fun setActiveWallet(walletId: String) {
        dataStore.edit { it[ACTIVE_WALLET_ID_KEY] = walletId }
    }

    override suspend fun createWallet(name: String, seedPhrase: SeedPhrase): Wallet =
        insertWallet(name.ifBlank { "My Wallet" }, seedPhrase)

    override suspend fun importWallet(name: String, seedPhrase: SeedPhrase): Wallet =
        insertWallet(name, seedPhrase)

    private suspend fun insertWallet(name: String, seedPhrase: SeedPhrase): Wallet {
        val wallet = Wallet(id = UUID.randomUUID().toString(), name = name)
        walletDao.upsert(wallet.toEntity())
        secureStorage.put(seedKey(wallet.id), seedPhrase.toDisplayString())
        dataStore.edit { it[ACTIVE_WALLET_ID_KEY] = wallet.id }
        return wallet
    }

    override suspend fun updateWalletName(id: String, name: String) {
        val entity = walletDao.getById(id) ?: return
        walletDao.upsert(entity.copy(name = name))
    }

    override suspend fun deleteWallet(id: String) {
        walletDao.deleteById(id)
        secureStorage.remove(seedKey(id))
        dataStore.edit { prefs ->
            if (prefs[ACTIVE_WALLET_ID_KEY] == id) prefs.remove(ACTIVE_WALLET_ID_KEY)
        }
    }

    override suspend fun getWalletCount(): Int = walletDao.getCount()

    override suspend fun getSeedPhrase(walletId: String): SeedPhrase? =
        secureStorage.get(seedKey(walletId))?.let { SeedPhrase(it.split(" ")) }

    private fun seedKey(walletId: String) = "seed_$walletId"

    private companion object {
        val ACTIVE_WALLET_ID_KEY = stringPreferencesKey("active_wallet_id")
    }
}

private fun WalletEntity.toWallet() = Wallet(id, name, createdAt)
private fun Wallet.toEntity() = WalletEntity(id, name, createdAt)
