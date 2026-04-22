package com.yasashny.fortera.core.walletbalances

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.AddressResolver
import kotlinx.coroutines.flow.first

/**
 * Resolves a wallet's on-chain addresses without exposing seed phrases to callers.
 *
 * Features that need an address (e.g. the Receive screen showing a QR code) should call
 * this instead of pulling the seed phrase themselves. Seed access stays contained inside
 * the bridge module.
 */
interface WalletAddressesService {
    /** Addresses for the given wallet, or null if the wallet / seed phrase is unavailable. */
    suspend fun forWallet(walletId: String): WalletAddresses?

    /** Addresses for the currently-active wallet, or null if there is no active wallet. */
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
