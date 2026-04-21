package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.AddressResolver
import com.yasashny.fortera.core.domaincrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.datasource.InfuraDataSource
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.network.environment.EnvironmentRepository
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction as BtcTransaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionWitness
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.ScriptBuilder
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

interface SendTransactionRepository {
    suspend fun estimateFees(token: TokenDefinition): Result<FeeEstimates>
    suspend fun send(
        mnemonic: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): Result<String>
}

internal class SendTransactionRepositoryImpl(
    private val infuraDataSource: InfuraDataSource,
    private val blockstreamDataSource: BlockstreamDataSource,
    private val addressResolver: AddressResolver,
    private val environmentRepository: EnvironmentRepository,
) : SendTransactionRepository {

    override suspend fun estimateFees(token: TokenDefinition): Result<FeeEstimates> = runCatching {
        when (token.network) {
            BlockchainNetwork.ETHEREUM -> estimateEthFees(token)
            BlockchainNetwork.BITCOIN -> estimateBtcFees()
        }
    }

    override suspend fun send(
        mnemonic: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): Result<String> = runCatching {
        when (token.network) {
            BlockchainNetwork.ETHEREUM -> sendEthereum(mnemonic, token, toAddress, amount, speed)
            BlockchainNetwork.BITCOIN -> sendBitcoin(mnemonic, toAddress, amount, speed)
        }
    }

    // ========== Ethereum ==========

    private suspend fun estimateEthFees(token: TokenDefinition): FeeEstimates {
        val gasPrice = infuraDataSource.getGasPrice()
        val gasLimit = ethGasLimit(token)
        return FeeSpeed.entries.associateWith { speed ->
            val scaled = gasPrice
                .multiply(BigInteger.valueOf((ethMultiplier(speed) * 100).toLong()))
                .divide(HUNDRED)
            val wei = scaled.multiply(gasLimit)
            FeeEstimate(
                nativeAmount = wei.toBigDecimal().movePointLeft(WEI_DECIMALS).stripTrailingZeros(),
                nativeSymbol = "ETH",
            )
        }
    }

    private suspend fun sendEthereum(
        mnemonic: String,
        token: TokenDefinition,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): String {
        val keys = addressResolver.ethKeys(mnemonic)
        val credentials = keys.credentials
        val chainId = environmentRepository.current().ethChainId

        val baseGasPrice = infuraDataSource.getGasPrice()
        val gasPrice = baseGasPrice
            .multiply(BigInteger.valueOf((ethMultiplier(speed) * 100).toLong()))
            .divide(HUNDRED)
        val gasLimit = ethGasLimit(token)
        val nonce = infuraDataSource.getNonce(credentials.address)

        val raw = if (token.contractAddress != null) {
            val decimals = ERC20_DECIMALS[token.symbol] ?: 18
            val rawAmount = amount.movePointRight(decimals).toBigInteger()
            val function = Function(
                "transfer",
                listOf(Address(toAddress), Uint256(rawAmount)),
                listOf(TypeReference.create(Bool::class.java)),
            )
            val data = FunctionEncoder.encode(function)
            RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                token.contractAddress,
                BigInteger.ZERO,
                data,
            )
        } else {
            val valueWei = amount.movePointRight(WEI_DECIMALS).toBigInteger()
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, valueWei)
        }

        val signed = TransactionEncoder.signMessage(raw, chainId, credentials)
        return infuraDataSource.sendRawTransaction(Numeric.toHexString(signed))
    }

    private fun ethGasLimit(token: TokenDefinition): BigInteger =
        if (token.contractAddress != null) ERC20_GAS_LIMIT else ETH_TRANSFER_GAS_LIMIT

    private fun ethMultiplier(speed: FeeSpeed): Double = when (speed) {
        FeeSpeed.SLOW -> 0.85
        FeeSpeed.FAST -> 1.0
        FeeSpeed.INSTANT -> 1.4
    }

    // ========== Bitcoin ==========

    private suspend fun estimateBtcFees(): FeeEstimates {
        val estimates = blockstreamDataSource.getFeeEstimates()
        val satPerVb = mapOf(
            FeeSpeed.SLOW to pickFeeRate(estimates, 144),
            FeeSpeed.FAST to pickFeeRate(estimates, 6),
            FeeSpeed.INSTANT to pickFeeRate(estimates, 1),
        )
        return satPerVb.mapValues { (_, rate) ->
            val fee = (rate * BTC_VSIZE_ESTIMATE).toLong().coerceAtLeast(BTC_MIN_FEE_SATS)
            FeeEstimate(
                nativeAmount = fee.toBigDecimal().movePointLeft(SATS_DECIMALS).stripTrailingZeros(),
                nativeSymbol = "BTC",
            )
        }
    }

    private suspend fun sendBitcoin(
        mnemonic: String,
        toAddress: String,
        amount: BigDecimal,
        speed: FeeSpeed,
    ): String {
        val params = addressResolver.btcParams()
        val keys = addressResolver.btcKeys(mnemonic)
        val ecKey = keys.ecKey
        val utxos = blockstreamDataSource.getUtxos(keys.address).filter { it.confirmed }
        require(utxos.isNotEmpty()) { "No confirmed UTXOs available" }

        val estimates = blockstreamDataSource.getFeeEstimates()
        val feeRate = when (speed) {
            FeeSpeed.SLOW -> pickFeeRate(estimates, 144)
            FeeSpeed.FAST -> pickFeeRate(estimates, 6)
            FeeSpeed.INSTANT -> pickFeeRate(estimates, 1)
        }

        val amountSats = amount.movePointRight(SATS_DECIMALS).toLong()
        val sorted = utxos.sortedByDescending { it.valueSats }
        val selected = mutableListOf<BlockstreamDataSource.Utxo>()
        var inputSum = 0L
        for (utxo in sorted) {
            selected.add(utxo)
            inputSum += utxo.valueSats
            val vsize = BTC_VSIZE_BASE + BTC_VSIZE_PER_INPUT * selected.size + BTC_VSIZE_PER_OUTPUT * 2
            val fee = (feeRate * vsize).toLong().coerceAtLeast(BTC_MIN_FEE_SATS)
            if (inputSum >= amountSats + fee) break
        }

        val vsize = BTC_VSIZE_BASE + BTC_VSIZE_PER_INPUT * selected.size + BTC_VSIZE_PER_OUTPUT * 2
        val fee = (feeRate * vsize).toLong().coerceAtLeast(BTC_MIN_FEE_SATS)
        require(inputSum >= amountSats + fee) { "Insufficient funds for amount + fee" }

        val tx = BtcTransaction(params)
        val recipient = SegwitAddress.fromBech32(params, toAddress)
        tx.addOutput(Coin.valueOf(amountSats), recipient)
        val change = inputSum - amountSats - fee
        if (change >= BTC_DUST_THRESHOLD) {
            val changeAddress = SegwitAddress.fromKey(params, ecKey)
            tx.addOutput(Coin.valueOf(change), changeAddress)
        }

        selected.forEach { utxo ->
            val outPoint = TransactionOutPoint(params, utxo.vout.toLong(), Sha256Hash.wrap(utxo.txid))
            val input = TransactionInput(
                params,
                tx,
                ByteArray(0),
                outPoint,
                Coin.valueOf(utxo.valueSats),
            )
            tx.addInput(input)
        }

        val scriptCode = ScriptBuilder.createP2PKHOutputScript(ecKey)
        selected.forEachIndexed { index, utxo ->
            val sighash = tx.hashForWitnessSignature(
                index,
                scriptCode,
                Coin.valueOf(utxo.valueSats),
                BtcTransaction.SigHash.ALL,
                false,
            )
            val signature = ecKey.sign(sighash)
            val txSig = TransactionSignature(signature, BtcTransaction.SigHash.ALL, false)
            tx.getInput(index.toLong()).witness = TransactionWitness.redeemP2WPKH(txSig, ecKey)
        }

        val rawHex = Numeric.toHexStringNoPrefix(tx.bitcoinSerialize())
        return blockstreamDataSource.broadcastTransaction(rawHex)
    }

    private fun pickFeeRate(estimates: Map<Int, Double>, target: Int): Double {
        val exact = estimates[target]
        if (exact != null) return exact
        val best = estimates.entries
            .filter { it.key <= target }
            .maxByOrNull { it.key }
            ?.value
        return best ?: estimates.values.firstOrNull() ?: DEFAULT_BTC_SAT_PER_VB
    }

    companion object {
        private const val WEI_DECIMALS = 18
        private const val SATS_DECIMALS = 8
        private val ETH_TRANSFER_GAS_LIMIT = BigInteger.valueOf(21_000)
        private val ERC20_GAS_LIMIT = BigInteger.valueOf(65_000)
        private val HUNDRED = BigInteger.valueOf(100)

        private const val BTC_VSIZE_BASE = 11
        private const val BTC_VSIZE_PER_INPUT = 68
        private const val BTC_VSIZE_PER_OUTPUT = 31
        private const val BTC_VSIZE_ESTIMATE = BTC_VSIZE_BASE + BTC_VSIZE_PER_INPUT + BTC_VSIZE_PER_OUTPUT * 2
        private const val BTC_MIN_FEE_SATS = 200L
        private const val BTC_DUST_THRESHOLD = 546L
        private const val DEFAULT_BTC_SAT_PER_VB = 1.0

        private val ERC20_DECIMALS = mapOf(
            "USDT" to 6,
            "USDC" to 6,
        )
    }
}
