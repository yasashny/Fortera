package com.yasashny.fortera.core.datacrypto.repository.send

import com.yasashny.fortera.core.datacrypto.AddressResolverImpl
import com.yasashny.fortera.core.datacrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
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

internal class EthereumSender(
    private val infuraDataSource: InfuraDataSource,
    private val addressResolver: AddressResolverImpl,
    private val environmentRepository: EnvironmentRepository,
) {

    suspend fun estimateFees(
        token: TokenDefinition,
        fromAddress: String?,
        amount: BigDecimal,
    ): FeeEstimates {
        val pricing = resolvePricing()
        val gasLimit = resolveGasLimit(token, fromAddress, amount)

        return FeeSpeed.entries.associateWith { speed ->
            val totalWei = pricing.maxFeePerGas(speed).multiply(gasLimit)
            FeeEstimate(
                nativeAmount = totalWei.toBigDecimal().movePointLeft(WEI_DECIMALS).stripTrailingZeros(),
                nativeSymbol = BlockchainNetwork.ETHEREUM.nativeSymbol,
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
        val nonce = infuraDataSource.getNonce(credentials.address)

        val pricing = resolvePricing()
        val gasLimit = resolveGasLimit(token, credentials.address, amount)

        val raw = pricing.buildTransaction(
            nonce = nonce,
            chainId = chainId,
            gasLimit = gasLimit,
            speed = speed,
            token = token,
            toAddress = toAddress,
            amount = amount,
        )

        val signed = TransactionEncoder.signMessage(raw, chainId, credentials)
        return infuraDataSource.sendRawTransaction(Numeric.toHexString(signed))
    }

    private suspend fun resolvePricing(): GasPricing = runCatching {
        val history = infuraDataSource.getFeeHistory(
            blockCount = FEE_HISTORY_BLOCKS,
            rewardPercentiles = PERCENTILES,
        )
        val baseFee = history.nextBaseFee.takeIf { it > BigInteger.ZERO } ?: error("empty fee history")
        val slowTip = history.medianPriorityFee(0).coerceAtLeastTip()
        val fastTip = history.medianPriorityFee(1).coerceAtLeastTip()
        val instantTip = history.medianPriorityFee(2).coerceAtLeastTip()
        GasPricing.Eip1559(baseFee, slowTip, fastTip, instantTip)
    }.getOrElse {
        GasPricing.Legacy(infuraDataSource.getGasPrice())
    }

    private suspend fun resolveGasLimit(
        token: TokenDefinition,
        fromAddress: String?,
        amount: BigDecimal,
    ): BigInteger {
        val contract = token.contractAddress ?: return ETH_TRANSFER_GAS_LIMIT

        val caller = fromAddress ?: ZERO_ADDRESS
        val estimated = runCatching {
            val rawAmount = amount.movePointRight(token.decimals).toBigInteger()
                .takeIf { it > BigInteger.ZERO } ?: BigInteger.ONE
            val data = encodeErc20TransferData(caller, rawAmount)
            infuraDataSource.estimateGas(from = caller, to = contract, data = data)
        }.getOrNull() ?: ERC20_FALLBACK_GAS_LIMIT

        return estimated.multiply(BigInteger.valueOf(120)).divide(HUNDRED)
    }

    private fun encodeErc20TransferData(toAddress: String, rawAmount: BigInteger): String {
        val function = Function(
            "transfer",
            listOf(Address(toAddress), Uint256(rawAmount)),
            listOf(TypeReference.create(Bool::class.java)),
        )
        return FunctionEncoder.encode(function)
    }

    private fun BigInteger.coerceAtLeastTip(): BigInteger =
        if (this <= BigInteger.ZERO) MIN_PRIORITY_FEE_WEI else this

    private sealed interface GasPricing {

        fun maxFeePerGas(speed: FeeSpeed): BigInteger

        fun buildTransaction(
            nonce: BigInteger,
            chainId: Long,
            gasLimit: BigInteger,
            speed: FeeSpeed,
            token: TokenDefinition,
            toAddress: String,
            amount: BigDecimal,
        ): RawTransaction

        data class Eip1559(
            val nextBaseFee: BigInteger,
            val slowTip: BigInteger,
            val fastTip: BigInteger,
            val instantTip: BigInteger,
        ) : GasPricing {

            fun tipFor(speed: FeeSpeed): BigInteger = when (speed) {
                FeeSpeed.SLOW -> slowTip
                FeeSpeed.FAST -> fastTip
                FeeSpeed.INSTANT -> instantTip
            }

            override fun maxFeePerGas(speed: FeeSpeed): BigInteger {
                return nextBaseFee.multiply(BigInteger.TWO).add(tipFor(speed))
            }

            override fun buildTransaction(
                nonce: BigInteger,
                chainId: Long,
                gasLimit: BigInteger,
                speed: FeeSpeed,
                token: TokenDefinition,
                toAddress: String,
                amount: BigDecimal,
            ): RawTransaction {
                val maxPriorityFee = tipFor(speed)
                val maxFee = maxFeePerGas(speed)
                val contract = token.contractAddress

                return if (contract != null) {
                    val rawAmount = amount.movePointRight(token.decimals).toBigInteger()
                    val data = FunctionEncoder.encode(
                        Function(
                            "transfer",
                            listOf(Address(toAddress), Uint256(rawAmount)),
                            listOf(TypeReference.create(Bool::class.java)),
                        )
                    )
                    RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        gasLimit,
                        contract,
                        BigInteger.ZERO,
                        data,
                        maxPriorityFee,
                        maxFee,
                    )
                } else {
                    val valueWei = amount.movePointRight(WEI_DECIMALS).toBigInteger()
                    RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        gasLimit,
                        toAddress,
                        valueWei,
                        "",
                        maxPriorityFee,
                        maxFee,
                    )
                }
            }
        }

        data class Legacy(val baseGasPrice: BigInteger) : GasPricing {
            override fun maxFeePerGas(speed: FeeSpeed): BigInteger {
                val multiplier = when (speed) {
                    FeeSpeed.SLOW -> 90
                    FeeSpeed.FAST -> 110
                    FeeSpeed.INSTANT -> 150
                }
                return baseGasPrice.multiply(BigInteger.valueOf(multiplier.toLong())).divide(HUNDRED)
            }

            override fun buildTransaction(
                nonce: BigInteger,
                chainId: Long,
                gasLimit: BigInteger,
                speed: FeeSpeed,
                token: TokenDefinition,
                toAddress: String,
                amount: BigDecimal,
            ): RawTransaction {
                val gasPrice = maxFeePerGas(speed)
                val contract = token.contractAddress

                return if (contract != null) {
                    val rawAmount = amount.movePointRight(token.decimals).toBigInteger()
                    val data = FunctionEncoder.encode(
                        Function(
                            "transfer",
                            listOf(Address(toAddress), Uint256(rawAmount)),
                            listOf(TypeReference.create(Bool::class.java)),
                        )
                    )
                    RawTransaction.createTransaction(
                        nonce, gasPrice, gasLimit, contract, BigInteger.ZERO, data,
                    )
                } else {
                    val valueWei = amount.movePointRight(WEI_DECIMALS).toBigInteger()
                    RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, valueWei)
                }
            }
        }
    }

    private companion object {
        const val WEI_DECIMALS = 18
        const val FEE_HISTORY_BLOCKS = 10
        val PERCENTILES = listOf(10, 50, 90)

        val ETH_TRANSFER_GAS_LIMIT: BigInteger = BigInteger.valueOf(21_000)
        val ERC20_FALLBACK_GAS_LIMIT: BigInteger = BigInteger.valueOf(90_000)
        val HUNDRED: BigInteger = BigInteger.valueOf(100)
        val MIN_PRIORITY_FEE_WEI: BigInteger = BigInteger.valueOf(1_000_000_000L)

        const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
    }
}
