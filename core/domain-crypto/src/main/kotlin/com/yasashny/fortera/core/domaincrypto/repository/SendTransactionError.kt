package com.yasashny.fortera.core.domaincrypto.repository

/**
 * Typed failures raised by send-transaction flows so presentation code can produce
 * localised messages without string matching.
 *
 * Any unexpected exception from the sender / data sources propagates as-is.
 */
sealed class SendTransactionError(message: String) : RuntimeException(message) {

    /** Bitcoin wallet has no confirmed UTXOs — nothing to spend. */
    data object NoConfirmedUtxos : SendTransactionError("No confirmed UTXOs available")

    /** Selected UTXOs can't cover the amount + network fee. */
    data object InsufficientFunds : SendTransactionError("Insufficient funds for amount + fee")
}
