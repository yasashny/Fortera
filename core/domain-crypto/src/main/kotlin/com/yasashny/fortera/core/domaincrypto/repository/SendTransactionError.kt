package com.yasashny.fortera.core.domaincrypto.repository

sealed class SendTransactionError(message: String) : RuntimeException(message) {

    data object NoConfirmedUtxos : SendTransactionError("No confirmed UTXOs available")

    data object InsufficientFunds : SendTransactionError("Insufficient funds for amount + fee")
}
