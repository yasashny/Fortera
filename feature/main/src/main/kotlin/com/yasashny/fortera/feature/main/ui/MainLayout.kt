package com.yasashny.fortera.feature.main.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.ForteraCollapsingScaffold
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.ShimmerBox
import com.yasashny.fortera.core.ui.component.cardGroupItems
import com.yasashny.fortera.core.ui.component.cardPosition
import com.yasashny.fortera.feature.main.presentation.MainContract
import java.math.BigDecimal
import java.util.Locale
import com.yasashny.fortera.feature.main.R as MainR

private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)

private fun tokenIconUrl(symbol: String): String =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${symbol.lowercase()}.png"

@Composable
private fun pulsingAlpha(enabled: Boolean): Float {
    if (!enabled) return 1f
    val transition = rememberInfiniteTransition(label = "cachedPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cachedAlpha",
    )
    return alpha
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainLayout(
    state: MainContract.State,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {},
    onWalletSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onManageTokensClick: () -> Unit,
    onTokenClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    val cachedAlpha = pulsingAlpha(state.isCached)

    Box {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
        ) {
        ForteraCollapsingScaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            bottomContentPadding = 32.dp,
            backgroundImage = MainR.drawable.bacgroung_gradiend,
            collapsedBar = {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.Start) {
                            if (state.activeWalletName != null) {
                                Text(
                                    text = state.activeWalletName,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            } else {
                                ShimmerBox(
                                    modifier = Modifier.size(width = 100.dp, height = 18.dp),
                                )
                            }
                            if (!state.isLoading) {
                                Text(
                                    text = formatUsd(state.totalUsd),
                                    modifier = Modifier.alpha(cachedAlpha),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            } else {
                                ShimmerBox(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(width = 70.dp, height = 14.dp),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(

                    ),
                )
            },
            expandedHeader = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onWalletSelectorClick)
                                .padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (state.activeWalletName != null) {
                                Text(
                                    text = state.activeWalletName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                ShimmerBox(
                                    modifier = Modifier.size(width = 120.dp, height = 28.dp),
                                )
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(MainR.string.main_settings_cd),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (!state.isLoading) {
                        Text(
                            text = formatUsd(state.totalUsd),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp, bottom = 60.dp)
                                .alpha(cachedAlpha),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 84.dp, bottom = 68.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ShimmerBox(
                                modifier = Modifier.size(width = 200.dp, height = 56.dp),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }
            },
        ) {
            item(key = "actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    ActionButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        label = stringResource(MainR.string.main_send),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 4.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        ),
                        onClick = onSendClick,
                    )
                    Spacer(Modifier.width(4.dp))
                    ActionButton(
                        icon = Icons.Default.QrCodeScanner,
                        label = stringResource(MainR.string.main_receive),
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 18.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 18.dp
                        ),
                        onClick = onReceiveClick,
                    )
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
            }

            if (state.isLoading) {
                items(4, key = { "shimmer_$it" }) { index ->
                    val position = cardPosition(index, 4)
                    if (index > 0) {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    ShimmerGroupCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        position = position,
                    )
                }
            } else {
                cardGroupItems(
                    items = state.tokens,
                    key = { it.token.id },
                ) { tokenBalance, position ->
                    val change = tokenBalance.changePercent24h
                    val changeColor = when {
                        change > 0 -> ChangePositive
                        change < 0 -> ChangeNegative
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val networkBadgeUrl = if (tokenBalance.token.contractAddress != null) {
                        tokenIconUrl("eth")
                    } else {
                        null
                    }
                    GroupCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        position = position,
                        onClick = { onTokenClick(tokenBalance.token.id) },
                        title = tokenBalance.token.name,
                        subtitle = String.format(Locale.US, "%+.2f%%", change),
                        subtitleColor = changeColor.copy(alpha = cachedAlpha),
                        iconUrl = tokenIconUrl(tokenBalance.token.symbol),
                        icon = CardIcon.Letter(tokenBalance.token.symbol.first()),
                        badgeIconUrl = networkBadgeUrl,
                        trailing = {
                            Text(
                                text = "${formatCrypto(tokenBalance.balance)} ${tokenBalance.token.symbol}",
                                modifier = Modifier.alpha(cachedAlpha),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                    )
                }

                item(key = "manage") {
                    Spacer(modifier = Modifier.height(32.dp))
                    TextButton(
                        onClick = onManageTokensClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(MainR.string.main_manage_tokens),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
        }

        ErrorBanner(
            modifier = Modifier.zIndex(1f),
            visible = errorMessage != null,
            message = errorMessage ?: "",
            onDismiss = onErrorDismiss,
        )
    }
}

private fun formatUsd(amount: Double): String {
    val formatter = java.text.DecimalFormat("#,##0.00")
    val symbols = formatter.decimalFormatSymbols.apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
    formatter.decimalFormatSymbols = symbols
    return "$${formatter.format(amount)}"
}

private fun formatCrypto(amount: BigDecimal): String {
    val plain = amount.toPlainString()
    val dotIndex = plain.indexOf('.')
    return if (dotIndex == -1 || plain.length - dotIndex <= 7) plain
    else plain.substring(0, dotIndex + 7)
}

@Preview(showBackground = true)
@Composable
private fun MainLayoutPreview() {
    ForteraTheme {
        MainLayout(
            state = MainContract.State(
                activeWalletName = "Wallet 1",
                totalUsd = 2557.4,
                tokens = emptyList(),
                isLoading = false,
            ),
            onWalletSelectorClick = {},
            onSettingsClick = {},
            onManageTokensClick = {},
            onTokenClick = {},
            onSendClick = {},
            onReceiveClick = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainLayoutLoadingPreview() {
    ForteraTheme {
        MainLayout(
            state = MainContract.State(
                isLoading = true,
            ),
            onWalletSelectorClick = {},
            onSettingsClick = {},
            onManageTokensClick = {},
            onTokenClick = {},
            onSendClick = {},
            onReceiveClick = {},
            onRefresh = {},
        )
    }
}
