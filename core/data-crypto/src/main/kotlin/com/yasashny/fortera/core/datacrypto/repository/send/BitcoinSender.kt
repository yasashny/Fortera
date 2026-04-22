package com.yasashny.fortera.core.datacrypto.repository.send

import com.yasashny.fortera.core.datacrypto.AddressResolverImpl
import com.yasashny.fortera.core.datacrypto.datasource.BlockstreamDataSource
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimates
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.core.domaincrypto.repository.SendTransactionError
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
 *
 * [estimateFees] pre-runs UTXO selection for the requested amount so the fee shown on the
 * confirm screen matches the one the user will actually pay at send time (within rounding).
 * Without this, a single-input vsize estimate would under-report the fee whenever the real
 * transaction needs more than one input.
 *
 * [pickFeeRate] linearly interpolates between neighbouring `block-target → sat/vbyte`
 * points when the exact target isn't present in the estimates map — avoids the "jumped
 * from 10-block rate to 1008-block rate" cliff on sparsely-populated testnets.
 */
internal class BitcoinSender(
    private val blockstreamDataSource: BlockstreamDataSource,
    private val addressResolver: AddressResolverImpl,
) {

    suspend fun estimateFees(btcAddress: String?, amount: BigDecimal): FeeEstimates {
        val estimates = blockstreamDataSource.getFeeEstimates()
        val amountSats = amount.movePointRight(SATS_DECIMALS).toLong().coerceAtLeast(1L)

        // Try to fetch real UTXOs for accurate vsize; fall back to 1-input estimate on failure.
        val utxos = btcAddress
            ?.let { runCatching { blockstreamDataSource.getUtxos(it).filter { u -> u.confirmed } }.getOrNull() }
            .orEmpty()

        return FeeSpeed.entries.associateWith { speed ->
            val rate = pickFeeRate(estimates, targetBlocks(speed))
            val vsize = estimateVsize(utxos, amountSats, rate)
            val fee = (rate * vsize).toLong().coerceAtLeast(MIN_FEE_SATS)
            FeeEstimate(
                nativeAmount = fee.toBigDecimal().movePointLeft(SATS_DECIMALS).stripTrailingZeros(),
                nativeSymbol = BlockchainNetwork.BITCOIN.nativeSymbol,
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
        if (utxos.isEmpty()) throw SendTransactionError.NoConfirmedUtxos

        val feeRate = pickFeeRate(blockstreamDataSource.getFeeEstimates(), targetBlocks(speed))
        val amountSats = amount.movePointRight(SATS_DECIMALS).toLong()

        val selected = selectUtxos(utxos, amountSats, feeRate)
        val inputSum = selected.sumOf { it.valueSats }
        val vsize = vsizeFor(selected.size)
        val fee = (feeRate * vsize).toLong().coerceAtLeast(MIN_FEE_SATS)
        if (inputSum < amountSats + fee) throw SendTransactionError.InsufficientFunds

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

    // ───────────────────────────── Helpers ─────────────────────────────

    /**
     * Estimates vsize of a transaction that would cover [targetSats] at the given [feeRate].
     * Falls back to a single-input estimate if we don't have UTXOs available.
     */
    private fun estimateVsize(
        utxos: List<BlockstreamDataSource.Utxo>,
        targetSats: Long,
        feeRate: Double,
    ): Int {
        if (utxos.isEmpty()) return VSIZE_ONE_INPUT
        val selected = selectUtxos(utxos, targetSats, feeRate)
        if (selected.isEmpty()) return VSIZE_ONE_INPUT
        return vsizeFor(selected.size)
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
        VSIZE_BASE + VSIZE_PER_INPUT * inputs.coerceAtLeast(1) + VSIZE_PER_OUTPUT * 2

    /**
     * Picks the fee rate for [target] blocks. Interpolates between neighbouring keys when
     * the exact target is missing.
     *
     * The API semantics: `rate[target]` is the minimum sat/vbyte that confirms within
     * `target` blocks. Smaller `target` → stricter deadline → higher rate. Interpolation
     * produces a reasonable in-between value even on sparse maps.
     */
    private fun pickFeeRate(estimates: Map<Int, Double>, target: Int): Double {
        if (estimates.isEmpty()) return DEFAULT_SAT_PER_VB
        estimates[target]?.let { return it }
        val sorted = estimates.entries.sortedBy { it.key }
        val below = sorted.lastOrNull { it.key < target }
        val above = sorted.firstOrNull { it.key > target }
        return when {
            below != null && above != null -> {
                val span = (above.key - below.key).toDouble()
                val offset = (target - below.key).toDouble()
                below.value + (above.value - below.value) * (offset / span)
            }
            below != null -> below.value
            above != null -> above.value
            else -> DEFAULT_SAT_PER_VB
        }
    }

    private companion object {
        const val SATS_DECIMALS = 8
        const val VSIZE_BASE = 11
        const val VSIZE_PER_INPUT = 68
        const val VSIZE_PER_OUTPUT = 31
        const val VSIZE_ONE_INPUT = VSIZE_BASE + VSIZE_PER_INPUT + VSIZE_PER_OUTPUT * 2
        const val MIN_FEE_SATS = 200L
        const val DUST_THRESHOLD = 546L
        const val DEFAULT_SAT_PER_VB = 1.0
    }
}
