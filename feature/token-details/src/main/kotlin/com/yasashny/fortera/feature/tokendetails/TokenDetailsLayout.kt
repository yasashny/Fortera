package com.yasashny.fortera.feature.tokendetails

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.areaSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.crosshairLineOptions
import com.tradingview.lightweightcharts.api.options.models.crosshairOptions
import com.tradingview.lightweightcharts.api.options.models.gridLineOptions
import com.tradingview.lightweightcharts.api.options.models.gridOptions
import com.tradingview.lightweightcharts.api.options.models.layoutOptions
import com.tradingview.lightweightcharts.api.options.models.priceScaleOptions
import com.tradingview.lightweightcharts.api.options.models.timeScaleOptions
import com.tradingview.lightweightcharts.api.series.enums.CrosshairMode
import com.tradingview.lightweightcharts.api.series.enums.LineStyle
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.series.models.AreaData
import com.tradingview.lightweightcharts.api.series.models.MouseEventParams
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
import com.yasashny.fortera.core.ui.component.ShimmerBox
import com.yasashny.fortera.core.ui.component.cardShapeForPosition
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.yasashny.fortera.core.ui.currency.LocalFiat
import com.yasashny.fortera.core.ui.format.formatCrypto
import com.yasashny.fortera.core.ui.format.formatFiat
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.feature.tokendetails.R as TokenDetailsR
import kotlin.math.abs

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
                    Text(
                        text = "${state.tokenName} ${state.tokenSymbol}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        var hoveredPrice by remember { mutableStateOf<Double?>(null) }
        val basePrice = state.priceHistory.firstOrNull()?.priceUsd

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            val shownPrice = hoveredPrice ?: state.priceUsd
            val shownChange = if (hoveredPrice != null && basePrice != null && basePrice > 0.0) {
                (hoveredPrice!! - basePrice) / basePrice * 100.0
            } else {
                state.changePercent24h
            }
            PriceHero(
                priceUsd = shownPrice,
                changePercent = shownChange,
                isLoading = state.isLoading,
            )

            Spacer(Modifier.height(16.dp))

            PriceChart(
                priceHistory = state.priceHistory,
                changePercent = state.changePercent24h,
                isLoading = state.isLoading || state.isChartLoading,
                onHoverChange = { hoveredPrice = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
            )

            Spacer(Modifier.height(8.dp))

            PeriodSelector(
                selected = state.selectedPeriod,
                onPeriodSelected = onPeriodSelected,
            )

            Spacer(Modifier.height(16.dp))

            val badgeUrl = if (state.tokenContractAddress != null) {
                tokenIconUrl("eth")
            } else {
                null
            }
            val fiatBalance = state.balance.toDouble() * state.priceUsd
            GroupCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                position = CardPosition.First,
                innerRadius = 4.dp,
                onClick = {},
                title = stringResource(TokenDetailsR.string.token_details_your_balance),
                subtitle = state.tokenName,
                iconUrl = state.tokenIconUrl,
                icon = state.tokenSymbol.firstOrNull()?.let { CardIcon.Letter(it) },
                badgeIconUrl = badgeUrl,
                trailing = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (state.isLoading) {
                            ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 16.dp))
                            ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 12.dp))
                        } else {
                            Text(
                                text = "${formatCrypto(state.balance)} ${state.tokenSymbol}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            val fiatText = formatFiat(fiatBalance, LocalFiat.current, approximate = true)
                            if (fiatText != null) {
                                Text(
                                    text = fiatText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 12.dp))
                            }
                        }
                    }
                },
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = stringResource(TokenDetailsR.string.token_details_send),
                    shape = RoundedCornerShape(
                        topStart = 4.dp, topEnd = 4.dp,
                        bottomStart = 16.dp, bottomEnd = 4.dp,
                    ),
                    onClick = onSendClick,
                )
                Spacer(Modifier.width(4.dp))
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.QrCodeScanner,
                    label = stringResource(TokenDetailsR.string.token_details_receive),
                    shape = RoundedCornerShape(
                        topStart = 4.dp, topEnd = 4.dp,
                        bottomStart = 4.dp, bottomEnd = 16.dp,
                    ),
                    onClick = onReceiveClick,
                )
            }

            Spacer(Modifier.height(16.dp))

            TransactionsSection(
                transactions = state.transactions,
                isLoading = state.isLoading || state.isTransactionsLoading,
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
        val shimmerCount = 3
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(shimmerCount) { index ->
                val position = when {
                    shimmerCount == 1 -> CardPosition.Single
                    index == 0 -> CardPosition.First
                    index == shimmerCount - 1 -> CardPosition.Last
                    else -> CardPosition.Middle
                }
                ShimmerTransactionCard(
                    position = position,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}

@Composable
private fun ShimmerTransactionCard(
    position: CardPosition,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = cardShapeForPosition(position, innerRadius = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 16.dp))
                ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 14.dp))
                ShimmerBox(modifier = Modifier.size(width = 50.dp, height = 12.dp))
            }
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
        innerRadius = 4.dp,
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
private fun PriceHero(
    priceUsd: Double,
    changePercent: Double,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        val fiat = LocalFiat.current
        val priceText = if (isLoading) null else formatFiat(priceUsd, fiat)
        if (priceText == null) {
            ShimmerBox(modifier = Modifier.size(width = 180.dp, height = 40.dp))
            ShimmerBox(
                modifier = Modifier.size(width = 140.dp, height = 22.dp),
                shape = RoundedCornerShape(50),
            )
            return@Column
        }

        Text(
            text = priceText,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.5).sp,
        )

        if (priceUsd > 0.0) {
            val isUp = changePercent >= 0
            val color = if (isUp) ChangePositive else ChangeNegative
            val deltaAbs = abs(priceUsd * changePercent / (100.0 + changePercent))
            val sign = if (isUp) "+" else "−"
            val pctFormatted = String.format(Locale.US, "%.2f", abs(changePercent))
            val deltaFormatted = formatFiat(deltaAbs, fiat) ?: return@Column

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (isUp) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "$sign$pctFormatted% · $sign$deltaFormatted",
                    color = color,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun PriceChart(
    priceHistory: List<PricePoint>,
    changePercent: Double,
    isLoading: Boolean,
    onHoverChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (priceHistory.isEmpty()) {
        if (isLoading) {
            ShimmerBox(
                modifier = modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
            )
        } else {
            Box(modifier = modifier)
        }
        return
    }

    val bgColor = MaterialTheme.colorScheme.background.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()

    val isUp = changePercent >= 0
    val trendColor = if (isUp) ChangePositive else ChangeNegative
    val trendArgb = trendColor.toArgb()
    val trendFillTopArgb = trendColor.copy(alpha = 0.24f).toArgb()

    val seriesRef = remember { arrayOfNulls<SeriesApi>(1) }
    val chartData = remember(priceHistory) {
        priceHistory.map { point ->
            AreaData(
                time = Time.Utc(point.timestampMs / 1000),
                value = point.priceUsd.toFloat(),
            )
        }
    }

    val hoverListener = remember(onHoverChange) {
        { params: MouseEventParams ->
            val value = params.seriesData?.firstOrNull()?.prices?.value?.toDouble()
            onHoverChange(value)
        }
    }

    fun seriesOptions() = areaSeriesOptions {
        topColor = trendFillTopArgb.toIntColor()
        bottomColor = Color.Transparent.toArgb().toIntColor()
        lineColor = trendArgb.toIntColor()
        lineWidth = LineWidth.TWO
        priceLineVisible = false
        baseLineVisible = false
        lastValueVisible = false
        crosshairMarkerVisible = true
        crosshairMarkerRadius = 5f
        crosshairMarkerBorderWidth = 2f
        crosshairMarkerBorderColor = surfaceColor.toIntColor()
        crosshairMarkerBackgroundColor = trendArgb.toIntColor()
    }

    AndroidView(
        factory = { context ->
            ChartsView(context).apply {
                api.applyOptions {
                    layout = layoutOptions {
                        background = SolidColor(bgColor)
                    }
                    grid = gridOptions {
                        vertLines = gridLineOptions { visible = false }
                        horzLines = gridLineOptions { visible = false }
                    }
                    rightPriceScale = priceScaleOptions {
                        visible = false
                        borderVisible = false
                    }
                    timeScale = timeScaleOptions {
                        visible = false
                        borderVisible = false
                    }
                    crosshair = crosshairOptions {
                        mode = CrosshairMode.MAGNET
                        vertLine = crosshairLineOptions {
                            color = trendArgb.toIntColor()
                            width = LineWidth.ONE
                            style = LineStyle.DASHED
                            labelVisible = false
                        }
                        horzLine = crosshairLineOptions {
                            visible = false
                            labelVisible = false
                        }
                    }
                }
                api.subscribeCrosshairMove(hoverListener)
                api.addAreaSeries(
                    options = seriesOptions(),
                    onSeriesCreated = { series ->
                        seriesRef[0] = series
                        series.setData(chartData)
                    }
                )
            }
        },
        update = { _ ->
            seriesRef[0]?.let { series ->
                series.applyOptions(seriesOptions())
                series.setData(chartData)
            }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
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
