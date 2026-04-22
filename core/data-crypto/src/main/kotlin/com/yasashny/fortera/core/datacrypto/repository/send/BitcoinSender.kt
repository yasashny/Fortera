package com.yasashny.fortera.core.datacrypto.repository.send

import com.yasashny.fortera.core.datacrypto.AddressResolverImpl
import com.yasashny.fortera.core.datacrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import org.bitcoinj.core.Coin
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionWitness
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.ScriptBuilder
import org.web3j.utils.Numeric
import java.math.BigDecimal

/**
 * Signs and broadcasts P2WPKH (SegWit) Bitcoin transactions.
 * Selects UTXOs greedily, calculates fee from estimated vsize, sends change back to the wallet key.
 */
internal class BitcoinSender(
    private val blockstreamDataSource: BlockstreamDataSource,
    private val addressResolver: AddressResolverImpl,
) {

    suspend fun estimateFees(): FeeEstimates {
        val estimates = blockstreamDataSource.getFeeEstimates()
        return FeeSpeed.entries.associateWith { speed ->
            val rate = pickFeeRate(estimates, targetBlocks(speed))
            val fee = (rate * VSIZE_ESTIMATE).toLong().coerceAtLeast(MIN_FEE_SATS)
            FeeEstimate(
                nativeAmount = fee.toBigDecimal().movePointLeft(SATS_DECIMALS).stripTrailingZeros(),
                nativeSymbol = "BTC",
            )
        }
    }

    suspend fun send(
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

        val feeRate = pickFeeRate(blockstreamDataSource.getFeeEstimates(), targetBlocks(speed))
        val amountSats = amount.movePointRight(SATS_DECIMALS).toLong()

        val selected = selectUtxos(utxos, amountSats, feeRate)
        val inputSum = selected.sumOf { it.valueSats }
        val vsize = vsizeFor(selected.size)
        val fee = (feeRate * vsize).toLong().coerceAtLeast(MIN_FEE_SATS)
        require(inputSum >= amountSats + fee) { "Insufficient funds for amount + fee" }

        val tx = Transaction(params)
        tx.addOutput(Coin.valueOf(amountSats), SegwitAddress.fromBech32(params, toAddress))
        val change = inputSum - amountSats - fee
        if (change >= DUST_THRESHOLD) {
            tx.addOutput(Coin.valueOf(change), SegwitAddress.fromKey(params, ecKey))
        }

        selected.forEach { utxo ->
            val outPoint = TransactionOutPoint(params, utxo.vout.toLong(), Sha256Hash.wrap(utxo.txid))
            tx.addInput(TransactionInput(params, tx, ByteArray(0), outPoint, Coin.valueOf(utxo.valueSats)))
        }

        val scriptCode = ScriptBuilder.createP2PKHOutputScript(ecKey)
        selected.forEachIndexed { index, utxo ->
            val sighash = tx.hashForWitnessSignature(
                index,
                scriptCode,
                Coin.valueOf(utxo.valueSats),
                Transaction.SigHash.ALL,
                false,
            )
            val txSig = TransactionSignature(ecKey.sign(sighash), Transaction.SigHash.ALL, false)
            tx.getInput(index.toLong()).witness = TransactionWitness.redeemP2WPKH(txSig, ecKey)
        }

        return blockstreamDataSource.broadcastTransaction(Numeric.toHexStringNoPrefix(tx.bitcoinSerialize()))
    }

    private fun selectUtxos(
        utxos: List<BlockstreamDataSource.Utxo>,
        targetSats: Long,
        feeRate: Double,
    ): List<BlockstreamDataSource.Utxo> {
        val sorted = utxos.sortedByDescending { it.valueSats }
        val chosen = mutableListOf<BlockstreamDataSource.Utxo>()
        var inputSum = 0L
        for (utxo in sorted) {
            chosen.add(utxo)
            inputSum += utxo.valueSats
            val fee = (feeRate * vsizeFor(chosen.size)).toLong().coerceAtLeast(MIN_FEE_SATS)
            if (inputSum >= targetSats + fee) break
        }
        return chosen
    }

    private fun targetBlocks(speed: FeeSpeed): Int = when (speed) {
        FeeSpeed.SLOW -> 144
        FeeSpeed.FAST -> 6
        FeeSpeed.INSTANT -> 1
    }

    private fun vsizeFor(inputs: Int): Int =
        VSIZE_BASE + VSIZE_PER_INPUT * inputs + VSIZE_PER_OUTPUT * 2

    private fun pickFeeRate(estimates: Map<Int, Double>, target: Int): Double {
        estimates[target]?.let { return it }
        val best = estimates.entries
            .filter { it.key <= target }
            .maxByOrNull { it.key }
            ?.value
        return best ?: estimates.values.firstOrNull() ?: DEFAULT_SAT_PER_VB
    }

    private companion object {
        const val SATS_DECIMALS = 8
        const val VSIZE_BASE = 11
        const val VSIZE_PER_INPUT = 68
        const val VSIZE_PER_OUTPUT = 31
        const val VSIZE_ESTIMATE = VSIZE_BASE + VSIZE_PER_INPUT + VSIZE_PER_OUTPUT * 2
        const val MIN_FEE_SATS = 200L
        const val DUST_THRESHOLD = 546L
        const val DEFAULT_SAT_PER_VB = 1.0
    }
}
