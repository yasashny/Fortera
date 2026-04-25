package com.yasashny.fortera.core.datacrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.repository.AddressValidator
import com.yasashny.fortera.core.network.environment.AppEnvironment
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

internal class AddressValidatorImpl(
    private val environmentRepository: EnvironmentRepository,
) : AddressValidator {

    override suspend fun isValid(address: String, network: BlockchainNetwork): Boolean {
        if (address.isBlank()) return false
        return runCatching {
            when (network) {
                BlockchainNetwork.ETHEREUM -> address.matches(ETH_REGEX)
                BlockchainNetwork.BITCOIN -> {
                    val params = when (environmentRepository.current()) {
                        AppEnvironment.MAINNET -> MainNetParams.get()
                        AppEnvironment.TESTNET -> TestNet3Params.get()
                    }
                    SegwitAddress.fromBech32(params, address)
                    true
                }
            }
        }.getOrDefault(false)
    }

    private companion object {
        val ETH_REGEX = Regex("^0x[a-fA-F0-9]{40}$")
    }
}
