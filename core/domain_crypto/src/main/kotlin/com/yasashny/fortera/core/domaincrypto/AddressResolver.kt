package com.yasashny.fortera.core.domaincrypto

import com.yasashny.fortera.core.network.environment.AppEnvironment
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

class AddressResolver(private val environmentRepository: EnvironmentRepository) {

    suspend fun ethAddress(mnemonic: String): String =
        HdWallet.deriveEthAddress(mnemonic)

    suspend fun btcAddress(mnemonic: String): String =
        HdWallet.deriveBtcAddress(mnemonic, btcParams())

    suspend fun ethKeys(mnemonic: String): EthKeys =
        HdWallet.ethKeys(mnemonic)

    suspend fun btcKeys(mnemonic: String): BtcKeys =
        HdWallet.btcKeys(mnemonic, btcParams())

    suspend fun btcParams(): NetworkParameters = environmentRepository.current().toBtcParams()
}

fun AppEnvironment.toBtcParams(): NetworkParameters = when (this) {
    AppEnvironment.MAINNET -> MainNetParams.get()
    AppEnvironment.TESTNET -> TestNet3Params.get()
}
