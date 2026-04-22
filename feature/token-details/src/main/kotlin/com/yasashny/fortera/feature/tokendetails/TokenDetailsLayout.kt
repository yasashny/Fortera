package com.yasashny.fortera.feature.tokendetails

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.areaSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.crosshairOptions
import com.tradingview.lightweightcharts.api.options.models.gridLineOptions
import com.tradingview.lightweightcharts.api.options.models.gridOptions
import com.tradingview.lightweightcharts.api.options.models.layoutOptions
import com.tradingview.lightweightcharts.api.options.models.priceScaleOptions
import com.tradingview.lightweightcharts.api.options.models.timeScaleOptions
import com.tradingview.lightweightcharts.api.series.enums.CrosshairMode
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.series.models.AreaData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.view.ChartsView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import com.yasashny.fortera.core.domaincrypto.model.Transaction
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.yasashny.fortera.core.ui.format.formatCrypto
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.feature.tokendetails.R as TokenDetailsR

private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TokenDetailsLayout(
    state: TokenDetailsContract.State,
    onBackClick: () -> Unit,
    onPeriodSelected: (TokenDetailsContract.ChartPeriod) -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "${state.tokenName} ${state.tokenSymbol}")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            PriceChart(
                priceHistory = state.priceHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
            )

            Spacer(Modifier.height(8.dp))

            PeriodSelector(
                selected = state.selectedPeriod,
                onPeriodSelected = onPeriodSelected,
            )

            Spacer(Modifier.height(16.dp))

            // Send / Receive buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = stringResource(TokenDetailsR.string.token_details_send),
                    shape = RoundedCornerShape(
                        topStart = 18.dp, topEnd = 4.dp,
                        bottomStart = 18.dp, bottomEnd = 4.dp,
                    ),
                    onClick = onSendClick,
                )
                Spacer(Modifier.width(4.dp))
                ActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = stringResource(TokenDetailsR.string.token_details_receive),
                    shape = RoundedCornerShape(
                        topStart = 4.dp, topEnd = 18.dp,
                        bottomStart = 4.dp, bottomEnd = 18.dp,
                    ),
                    onClick = onReceiveClick,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Token card
            val change = state.changePercent24h
            val changeColor = when {
                change > 0 -> ChangePositive
                change < 0 -> ChangeNegative
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val badgeUrl = if (state.tokenContractAddress != null) {
                tokenIconUrl("eth")
            } else {
                null
            }
            GroupCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                position = CardPosition.Single,
                onClick = {},
                title = state.tokenName,
                subtitle = String.format(Locale.US, "%+.2f%%", change),
                subtitleColor = changeColor,
                iconUrl = tokenIconUrl(state.tokenSymbol),
                icon = state.tokenSymbol.firstOrNull()?.let { CardIcon.Letter(it) },
                badgeIconUrl = badgeUrl,
                trailing = {
                    Text(
                        text = "${formatCrypto(state.balance)} ${state.tokenSymbol}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            // Transactions
            TransactionsSection(
                transactions = state.transactions,
                isLoading = state.isTransactionsLoading,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TransactionsSection(
    transactions: List<Transaction>,
    isLoading: Boolean,
) {
    if (!isLoading && transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(TokenDetailsR.string.token_details_no_transactions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Text(
        text = stringResource(TokenDetailsR.string.token_details_transactions),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
        )
    } else {
        transactions.forEachIndexed { index, tx ->
            val position = when {
                transactions.size == 1 -> CardPosition.Single
                index == 0 -> CardPosition.First
                index == transactions.lastIndex -> CardPosition.Last
                else -> CardPosition.Middle
            }
            TransactionCard(
                transaction = tx,
                position = position,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PeriodSelector(
    selected: TokenDetailsContract.ChartPeriod,
    onPeriodSelected: (TokenDetailsContract.ChartPeriod) -> Unit,
) {
    val options = TokenDetailsContract.ChartPeriod.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, period ->
            ToggleButton(
                checked = period == selected,
                onCheckedChange = { onPeriodSelected(period) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(text = stringResource(period.labelRes))
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    position: CardPosition,
    modifier: Modifier = Modifier,
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = if (transaction.timestampSeconds > 0) {
        dateFormat.format(Date(transaction.timestampSeconds * 1000))
    } else {
        stringResource(TokenDetailsR.string.token_details_tx_pending)
    }

    val icon = if (transaction.isIncoming) Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade
    val amountPrefix = if (transaction.isIncoming) "+" else "-"
    val amountColor = if (transaction.isIncoming) ChangePositive else MaterialTheme.colorScheme.onSurface
    val counterparty = if (transaction.isIncoming) transaction.from else transaction.to

    GroupCard(
        modifier = modifier,
        position = position,
        onClick = {},
        title = if (transaction.isIncoming) {
            stringResource(TokenDetailsR.string.token_details_tx_received)
        } else {
            stringResource(TokenDetailsR.string.token_details_tx_sent)
        },
        subtitle = dateStr,
        icon = com.yasashny.fortera.core.ui.component.CardIcon.Vector(icon),
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${formatCrypto(transaction.amount)} ${transaction.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = amountColor,
                )
                Text(
                    text = shortenAddress(counterparty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}

private fun shortenAddress(address: String): String {
    if (address.length <= 13) return address
    return "${address.take(6)}...${address.takeLast(4)}"
}

@Composable
private fun PriceChart(
    priceHistory: List<PricePoint>,
    modifier: Modifier = Modifier,
) {
    if (priceHistory.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val bgColor = MaterialTheme.colorScheme.background.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    val seriesRef = remember { arrayOfNulls<SeriesApi>(1) }
    val chartData = remember(priceHistory) {
        priceHistory.map { point ->
            AreaData(
                time = Time.Utc(point.timestampMs / 1000),
                value = point.priceUsd.toFloat(),
            )
        }
    }

    AndroidView(
        factory = { context ->
            ChartsView(context).apply {
                api.applyOptions {
                    layout = layoutOptions {
                        background = SolidColor(bgColor)
                        this.textColor = textColor.toIntColor()
                    }
                    grid = gridOptions {
                        vertLines = gridLineOptions { visible = false }
                        horzLines = gridLineOptions {
                            color = gridColor.toIntColor()
                        }
                    }
                    rightPriceScale = priceScaleOptions {
                        visible = true
                        borderVisible = false
                    }
                    timeScale = timeScaleOptions {
                        visible = true
                        borderVisible = false
                        timeVisible = true
                    }
                    crosshair = crosshairOptions {
                        mode = CrosshairMode.NORMAL
                    }
                }
                api.addAreaSeries(
                    options = areaSeriesOptions {
                        topColor = Color(0x4002A64C).toArgb().toIntColor()
                        bottomColor = Color.Transparent.toArgb().toIntColor()
                        lineColor = ChangePositive.toArgb().toIntColor()
                        lineWidth = LineWidth.TWO
                    },
                    onSeriesCreated = { series ->
                        seriesRef[0] = series
                        series.setData(chartData)
                    }
                )
            }
        },
        update = { _ ->
            seriesRef[0]?.setData(chartData)
        },
        modifier = modifier,
    )
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

@Preview(showBackground = true)
@Composable
private fun TokenDetailsLayoutPreview() {
    ForteraTheme {
        TokenDetailsLayout(
            state = TokenDetailsContract.State(
                tokenName = "Ethereum",
                tokenSymbol = "ETH",
                balance = BigDecimal("1.234567"),
                priceUsd = 3200.0,
                changePercent24h = 2.45,
                isLoading = false,
                transactions = listOf(
                    Transaction(
                        hash = "0xabc123",
                        timestampSeconds = 1709900000,
                        from = "0x1234567890abcdef1234567890abcdef12345678",
                        to = "0xabcdef1234567890abcdef1234567890abcdef12",
                        amount = BigDecimal("0.05"),
                        symbol = "ETH",
                        isIncoming = false,
                        confirmed = true,
                    ),
                    Transaction(
                        hash = "0xdef456",
                        timestampSeconds = 1709800000,
                        from = "0xabcdef1234567890abcdef1234567890abcdef12",
                        to = "0x1234567890abcdef1234567890abcdef12345678",
                        amount = BigDecimal("0.1"),
                        symbol = "ETH",
                        isIncoming = true,
                        confirmed = true,
                    ),
                ),
            ),
            onBackClick = {},
            onPeriodSelected = {},
            onSendClick = {},
            onReceiveClick = {},
        )
    }
}
