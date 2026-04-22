package com.yasashny.fortera.feature.main.domain

import com.yasashny.fortera.core.domaincrypto.model.TokenBalance

data class MainOverview(
    val walletId: String,
    val balances: List<TokenBalance>,
    val totalUsd: Double,
    val unreachableNetworks: Set<String>,
) {
    val isFullyReachable: Boolean get() = unreachableNetworks.isEmpty()

    companion object {
        internal fun of(
            walletId: String,
            balances: List<TokenBalance>,
            unreachableNetworks: Set<String>,
        ): MainOverview = MainOverview(
            walletId = walletId,
            balances = balances,
            totalUsd = balances.sumOf { it.balanceUsd },
            unreachableNetworks = unreachableNetworks,
        )
    }
}
