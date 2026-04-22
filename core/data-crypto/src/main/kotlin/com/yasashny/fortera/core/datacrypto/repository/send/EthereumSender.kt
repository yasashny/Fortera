package com.yasashny.fortera.core.datacrypto.repository.send

import com.yasashny.fortera.core.datacrypto.AddressResolverImpl
import com.yasashny.fortera.core.datacrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Signs and broadcasts ETH / ERC-20 transactions. Pure network + crypto logic;
 * the outer [SendTransactionRepositoryImpl][com.yasashny.fortera.core.datacrypto.repository.SendTransactionRepositoryImpl]
 * just dispatches here for [com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork.ETHEREUM].
 */
internal class EthereumSender(
    private val infuraDataSource: InfuraDataSource,
    private val addressResolver: AddressResolverImpl,
    private val environmentRepository: EnvironmentRepository,
) {

    suspend fun estimateFees(token: TokenDefinition): FeeEstimates {
        val baseGasPrice = infuraDataSource.getGasPrice()
        val gasLimit = gasLimitFor(token)
        return FeeSpeed.entries.associateWith { speed ->
            val wei = gasPriceFor(baseGasPrice, speed).multiply(gasLimit)
            FeeEstimate(
                nativeAmount = wei.toBigDecimal().movePointLeft(WEI_DECIMALS).stripTrailingZeros(),
                nativeSymbol = "ETH",
            )
        }
    }

    suspend fun send(
        mnemonic: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): String {
        val credentials = addressResolver.ethKeys(mnemonic).credentials
        val chainId = environmentRepository.current().ethChainId

        val gasPrice = gasPriceFor(infuraDataSource.getGasPrice(), speed)
        val gasLimit = gasLimitFor(token)
        val nonce = infuraDataSource.getNonce(credentials.address)

        val raw = if (token.contractAddress != null) {
            val rawAmount = amount.movePointRight(token.decimals).toBigInteger()
            val function = Function(
                "transfer",
                listOf(Address(toAddress), Uint256(rawAmount)),
                listOf(TypeReference.create(Bool::class.java)),
            )
            RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                token.contractAddress,
                BigInteger.ZERO,
                FunctionEncoder.encode(function),
            )
        } else {
            val valueWei = amount.movePointRight(WEI_DECIMALS).toBigInteger()
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, valueWei)
        }

        val signed = TransactionEncoder.signMessage(raw, chainId, credentials)
        return infuraDataSource.sendRawTransaction(Numeric.toHexString(signed))
    }

    private fun gasLimitFor(token: TokenDefinition): BigInteger =
        if (token.contractAddress != null) ERC20_GAS_LIMIT else ETH_TRANSFER_GAS_LIMIT

    private fun gasPriceFor(base: BigInteger, speed: FeeSpeed): BigInteger {
        val multiplier = when (speed) {
            FeeSpeed.SLOW -> 85
            FeeSpeed.FAST -> 100
            FeeSpeed.INSTANT -> 140
        }
        return base.multiply(BigInteger.valueOf(multiplier.toLong())).divide(HUNDRED)
    }

    private companion object {
        const val WEI_DECIMALS = 18
        val ETH_TRANSFER_GAS_LIMIT: BigInteger = BigInteger.valueOf(21_000)
        val ERC20_GAS_LIMIT: BigInteger = BigInteger.valueOf(65_000)
        val HUNDRED: BigInteger = BigInteger.valueOf(100)
    }
}
