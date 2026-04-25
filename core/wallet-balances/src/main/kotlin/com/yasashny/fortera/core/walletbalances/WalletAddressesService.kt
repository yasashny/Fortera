package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import kotlinx.coroutines.flow.first

interface WalletAddressesService {
    suspend fun forWallet(walletId: String): WalletAddresses?

    suspend fun forActiveWallet(): WalletAddresses?
}

internal class WalletAddressesServiceImpl(
    private val walletInteractor: WalletInteractor,
    private val addressResolver: AddressResolver,
) : WalletAddressesService {

    override suspend fun forWallet(walletId: String): WalletAddresses? {
        val seed = walletInteractor.getSeedPhrase(walletId).getOrNull()?.toDisplayString()
            ?: return null
        return WalletAddresses(
            eth = addressResolver.ethAddress(seed),
            btc = addressResolver.btcAddress(seed),
        )
    }

    override suspend fun forActiveWallet(): WalletAddresses? {
        val wallet = walletInteractor.observeActiveWallet().first() ?: return null
        return forWallet(wallet.id)
    }
}
