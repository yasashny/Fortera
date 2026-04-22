package com.yasashny.fortera.feature.tokendetails

import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.BalanceRepository
import com.yasashny.fortera.core.domaincrypto.repository.PriceRepository
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.domaincrypto.repository.TransactionRepository
import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.walletbalances.WalletAddressesService
import com.yasashny.fortera.feature.tokendetails.TokenDetailsContract.ChartPeriod
import com.yasashny.fortera.feature.tokendetails.TokenDetailsContract.Effect
import com.yasashny.fortera.feature.tokendetails.TokenDetailsContract.Intent
import com.yasashny.fortera.feature.tokendetails.TokenDetailsContract.State
import kotlinx.coroutines.flow.first

class TokenDetailsViewModel(
    private val tokenId: String,
    private val walletInteractor: WalletInteractor,
    private val balanceRepository: BalanceRepository,
    private val priceRepository: PriceRepository,
    private val transactionRepository: TransactionRepository,
    private val tokenRepository: TokenRepository,
    private val walletAddressesService: WalletAddressesService,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var token: TokenDefinition? = null

    init {
        intent {
            token = tokenRepository.getTokenById(tokenId)
            val t = token
            if (t == null) {
                reduce(currentState.copy(isLoading = false))
                return@intent
            }
            reduce(
                currentState.copy(
                    tokenName = t.name,
                    tokenSymbol = t.symbol,
                    tokenContractAddress = t.contractAddress,
                )
            )

            launch { loadBalance() }
            launch { loadChartData(currentState.selectedPeriod) }
            launch { loadTransactions() }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.SelectPeriod -> {
                updateState { it.copy(selectedPeriod = intent.period, isChartLoading = true) }
                intent { launch { loadChartData(intent.period) } }
            }
            Intent.OpenReceive -> intent { sendEffect(Effect.NavigateToReceive(tokenId)) }
            Intent.OpenSend -> intent { sendEffect(Effect.NavigateToSend(tokenId)) }
        }
    }

    private suspend fun loadBalance() {
        val wallet = walletInteractor.observeActiveWallet().first()
        if (wallet == null) {
            reduce(currentState.copy(isLoading = false))
            return
        }
        val addresses = walletAddressesService.forWallet(wallet.id)
        if (addresses == null) {
            reduce(currentState.copy(isLoading = false))
            return
        }

        balanceRepository.getTokenBalances(wallet.id, addresses.eth, addresses.btc, setOf(tokenId))
            .onSuccess { result ->
                val tb = result.balances.find { it.token.id == tokenId }
                reduce(
                    currentState.copy(
                        balance = tb?.balance ?: currentState.balance,
                        priceUsd = tb?.priceUsd ?: currentState.priceUsd,
                        changePercent24h = tb?.changePercent24h ?: currentState.changePercent24h,
                        isLoading = false,
                    )
                )
            }
            .onFailure {
                reduce(currentState.copy(isLoading = false))
            }
    }

    private suspend fun loadTransactions() {
        val t = token ?: return
        updateState { it.copy(isTransactionsLoading = true) }

        val wallet = walletInteractor.observeActiveWallet().first() ?: run {
            updateState { it.copy(isTransactionsLoading = false) }
            return
        }
        val addresses = walletAddressesService.forWallet(wallet.id) ?: run {
            updateState { it.copy(isTransactionsLoading = false) }
            return
        }

        transactionRepository.getTransactions(t, addresses.eth, addresses.btc)
            .onSuccess { txs -> updateState { it.copy(transactions = txs, isTransactionsLoading = false) } }
            .onFailure { updateState { it.copy(isTransactionsLoading = false) } }
    }

    private suspend fun loadChartData(period: ChartPeriod) {
        val t = token ?: return
        updateState { it.copy(isChartLoading = true) }

        runCatching { priceRepository.getChart(t.id, period.days) }
            .onSuccess { points -> updateState { it.copy(priceHistory = points, isChartLoading = false) } }
            .onFailure { updateState { it.copy(isChartLoading = false) } }
    }
}
