package com.yasashny.fortera.core.datacrypto

import com.yasashny.fortera.core.domaincrypto.AddressResolver
import com.yasashny.fortera.core.network.environment.AppEnvironment
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

internal class AddressResolverImpl(
    private val environmentRepository: EnvironmentRepository,
) : AddressResolver {

    override fun ethAddress(mnemonic: String): String =
        HdWallet.deriveEthAddress(mnemonic)

    fun ethKeys(mnemonic: String): EthKeys =
        HdWallet.ethKeys(mnemonic)

    override suspend fun btcAddress(mnemonic: String): String =
        HdWallet.deriveBtcAddress(mnemonic, btcParams())

    suspend fun btcKeys(mnemonic: String): BtcKeys =
        HdWallet.btcKeys(mnemonic, btcParams())

    suspend fun btcParams(): NetworkParameters = environmentRepository.current().toBtcParams()
}

private fun AppEnvironment.toBtcParams(): NetworkParameters = when (this) {
    AppEnvironment.MAINNET -> MainNetParams.get()
    AppEnvironment.TESTNET -> TestNet3Params.get()
}
