package com.yasashny.fortera.core.domaincrypto.model

enum class FeeSpeed { SLOW, FAST, INSTANT }

data class FeeEstimate(
    val nativeAmount: java.math.BigDecimal,
    val nativeSymbol: String,
)

typealias FeeEstimates = Map<FeeSpeed, FeeEstimate>
