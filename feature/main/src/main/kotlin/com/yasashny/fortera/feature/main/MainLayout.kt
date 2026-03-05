package com.yasashny.fortera.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.ForteraCollapsingScaffold
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.cardGroupItems
import java.math.BigDecimal
import java.util.Locale
import com.yasashny.fortera.feature.main.R as MainR


private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)

private val BlockchainNetwork.displayName: String
    get() = when (this) {
        BlockchainNetwork.ETHEREUM -> "ERC-20"
        BlockchainNetwork.BITCOIN -> "BTC"
    }

private fun tokenIconUrl(symbol: String): String =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${symbol.lowercase()}.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainLayout(
    state: MainContract.State,
    onWalletSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onManageTokensClick: () -> Unit,
    onTokenClick: (String) -> Unit,
    onReceiveClick: () -> Unit,
    onRefresh: () -> Unit,
) {

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
                        Text(
                            text = state.activeWalletName ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = formatUsd(state.totalUsd),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                        Text(
                            text = state.activeWalletName ?: "—",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(MainR.string.main_settings_cd),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(
                    text = formatUsd(state.totalUsd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, bottom = 60.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
                    onClick = {},
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
                modifier = Modifier.padding(horizontal = 16.dp),
                position = position,
                onClick = { onTokenClick(tokenBalance.token.id) },
                title = tokenBalance.token.name,
                subtitle = String.format(Locale.US, "%+.2f%%", change),
                subtitleColor = changeColor,
                iconUrl = tokenIconUrl(tokenBalance.token.symbol),
                icon = CardIcon.Letter(tokenBalance.token.symbol.first()),
                badgeIconUrl = networkBadgeUrl,
                trailing = {
                    Text(
                        text = "${formatCrypto(tokenBalance.balance)} ${tokenBalance.token.symbol}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                },
            )
        }

        item(key = "manage") {
            Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    shape: Shape,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.size(100.dp, 78.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private fun formatUsd(amount: Double): String =
    String.format(Locale.US, "$%.2f", amount)

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
            onReceiveClick = {},
            onRefresh = {},
        )
    }
}
