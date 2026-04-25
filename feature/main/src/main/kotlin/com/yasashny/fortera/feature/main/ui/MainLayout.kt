package com.yasashny.fortera.feature.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.ForteraCollapsingScaffold
import com.yasashny.fortera.core.ui.component.ShimmerGroupCard
import com.yasashny.fortera.core.ui.component.cardGroupItems
import com.yasashny.fortera.core.ui.component.cardPosition
import com.yasashny.fortera.feature.main.R
import com.yasashny.fortera.feature.main.presentation.BalancesState
import com.yasashny.fortera.feature.main.presentation.MainIntent
import com.yasashny.fortera.feature.main.presentation.MainState
import com.yasashny.fortera.feature.main.ui.component.CollapsedBalanceBar
import com.yasashny.fortera.feature.main.ui.component.ExpandedBalanceHeader
import com.yasashny.fortera.feature.main.ui.component.ManageTokensButton
import com.yasashny.fortera.feature.main.ui.component.QuickActions
import com.yasashny.fortera.feature.main.ui.component.TokenRow
import com.yasashny.fortera.feature.main.ui.component.rememberStaleAlpha

private const val ShimmerItemCount = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainLayout(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
) {
    val isStale = (state.balances as? BalancesState.Ready)?.isStale == true
    val staleAlpha = rememberStaleAlpha(isStale)

    Box {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(MainIntent.Refresh) },
        ) {
            ForteraCollapsingScaffold(
                backgroundColor = MaterialTheme.colorScheme.background,
                bottomContentPadding = 32.dp,
                backgroundImage = R.drawable.bacgroung_gradiend,
                collapsedBar = {
                    CollapsedBalanceBar(
                        walletName = state.walletName,
                        balances = state.balances,
                        staleAlpha = staleAlpha,
                    )
                },
                expandedHeader = {
                    ExpandedBalanceHeader(
                        walletName = state.walletName,
                        balances = state.balances,
                        staleAlpha = staleAlpha,
                        onWalletSelectorClick = { onIntent(MainIntent.OpenWalletSelector) },
                        onSettingsClick = { onIntent(MainIntent.OpenSettings) },
                    )
                },
            ) {
                item(key = "actions") {
                    QuickActions(
                        onSendClick = { onIntent(MainIntent.OpenSend) },
                        onReceiveClick = { onIntent(MainIntent.OpenReceive) },
                    )
                }
                item(key = "actions_spacer") { Spacer(Modifier.height(32.dp)) }

                when (val balances = state.balances) {
                    BalancesState.Loading -> items(ShimmerItemCount, key = { "shimmer_$it" }) { index ->
                        val position = cardPosition(index, ShimmerItemCount)
                        if (index > 0) Spacer(modifier = Modifier.size(4.dp))
                        ShimmerGroupCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            position = position,
                        )
                    }

                    is BalancesState.Ready -> {
                        cardGroupItems(
                            items = balances.tokens,
                            key = { it.token.id },
                        ) { tokenBalance, position ->
                            TokenRow(
                                tokenBalance = tokenBalance,
                                position = position,
                                staleAlpha = staleAlpha,
                                onClick = { onIntent(MainIntent.OpenTokenDetails(tokenBalance.token.id)) },
                            )
                        }

                        item(key = "manage") {
                            Spacer(modifier = Modifier.height(32.dp))
                            ManageTokensButton(onClick = { onIntent(MainIntent.OpenManageTokens) })
                        }
                    }
                }
            }
        }

        ErrorBanner(
            banner = state.banner,
            onDismiss = { onIntent(MainIntent.DismissBanner) },
            modifier = Modifier.zIndex(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainLayoutLoadingPreview() {
    ForteraTheme {
        MainLayout(state = MainState.Initial, onIntent = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MainLayoutReadyPreview() {
    ForteraTheme {
        MainLayout(
            state = MainState(
                walletName = "Wallet 1",
                balances = BalancesState.Ready(
                    totalUsd = 2557.4,
                    tokens = emptyList(),
                    isStale = false,
                ),
            ),
            onIntent = {},
        )
    }
}
