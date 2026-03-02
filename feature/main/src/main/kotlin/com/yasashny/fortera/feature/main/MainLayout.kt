package com.yasashny.fortera.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.cryptoapi.model.TokenBalance
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.main.R as MainR
import java.math.BigDecimal
import java.util.Locale

private val BgColor = Color(0xFF0F0D13)
private val CardBg = Color(0xFF272A2E)
private val TextPrimary = Color(0xFFE0E2E8)
private val TextOnHeader = Color(0xFFE6E0E9)
private val ActionContainerColor = Color(0xFF394857)
private val ActionContentColor = Color(0xFFD4E4F6)
private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)
private val GlowColor = Color(0xFF6B2FBF)

private val CollapsedBarHeight = 64.dp

@Composable
internal fun MainLayout(
    state: MainContract.State,
    onWalletSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onManageTokensClick: () -> Unit,
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val statusBarPx = with(density) { statusBarHeight.toPx() }
    val collapsedBarPx = with(density) { (CollapsedBarHeight + statusBarHeight).toPx() }

    // 0f = header at rest (fully visible), 1f = header scrolled off behind collapsed bar
    val fraction by remember {
        derivedStateOf {
            val headerInfo = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == "header" }
            when {
                headerInfo == null && listState.firstVisibleItemIndex == 0 -> 0f
                headerInfo == null -> 1f
                else -> {
                    // How far header has scrolled from its initial position
                    val scrolled = statusBarPx - headerInfo.offset
                    // Max scroll = distance until header bottom reaches collapsed bar bottom
                    val maxScroll = headerInfo.size + statusBarPx - collapsedBarPx
                    if (maxScroll <= 0f) 0f
                    else (scrolled / maxScroll).coerceIn(0f, 1f)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
    ) {
        // Gradient glow — fades out as header scrolls away
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .alpha(1f - fraction)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GlowColor.copy(alpha = 0.55f), Color.Transparent),
                    )
                ),
        )

        // Scrollable content — header is the first item and scrolls naturally
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarHeight,
                bottom = 32.dp,
            ),
        ) {
            // ── EXPANDED HEADER — scrolls with content ──
            item(key = "header") {
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
                                color = TextOnHeader,
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextOnHeader,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(MainR.string.main_settings_cd),
                                tint = TextOnHeader,
                            )
                        }
                    }
                    Text(
                        text = formatUsd(state.totalUsd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )
                }
            }

            item(key = "actions") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.Send,
                        label = stringResource(MainR.string.main_send),
                        onClick = {},
                    )
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.QrCodeScanner,
                        label = stringResource(MainR.string.main_receive),
                        onClick = {},
                    )
                }
            }

            items(state.tokens, key = { it.token.id }) { tokenBalance ->
                TokenRow(tokenBalance = tokenBalance)
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
                        tint = TextOnHeader,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(MainR.string.main_manage_tokens),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnHeader,
                    )
                }
            }
        }

        // ── COLLAPSED BAR — fixed overlay, fades in as header scrolls away ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CollapsedBarHeight + statusBarHeight)
                .background(BgColor.copy(alpha = fraction))
                .alpha(fraction),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(CollapsedBarHeight),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.activeWalletName ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnHeader,
                    )
                    Text(
                        text = formatUsd(state.totalUsd),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ActionContainerColor),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ActionContentColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ActionContentColor,
            )
        }
    }
}

@Composable
private fun TokenRow(tokenBalance: TokenBalance) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tokenBalance.token.symbol.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = tokenBalance.token.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                val change = tokenBalance.changePercent24h
                Text(
                    text = String.format(Locale.US, "%+.2f%%", change),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change >= 0) ChangePositive else ChangeNegative,
                )
            }
            Text(
                text = "${formatCrypto(tokenBalance.balance)} ${tokenBalance.token.symbol}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.End,
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
        )
    }
}
