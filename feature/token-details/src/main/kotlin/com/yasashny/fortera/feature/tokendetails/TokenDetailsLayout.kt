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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domaincrypto.model.PricePoint
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import java.math.BigDecimal
import java.util.Locale
import com.yasashny.fortera.feature.tokendetails.R as TokenDetailsR

private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TokenDetailsLayout(
    state: TokenDetailsContract.State,
    onBackClick: () -> Unit,
    onPeriodSelected: (TokenDetailsContract.ChartPeriod) -> Unit,
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
            // Price chart
            PriceChart(
                priceHistory = state.priceHistory,
                isLoading = state.isChartLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            )

            Spacer(Modifier.height(8.dp))

            // Period selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                TokenDetailsContract.ChartPeriod.entries.forEachIndexed { index, period ->
                    SegmentedButton(
                        selected = period == state.selectedPeriod,
                        onClick = { onPeriodSelected(period) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TokenDetailsContract.ChartPeriod.entries.size,
                        ),
                    ) {
                        Text(text = period.label)
                    }
                }
            }

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
                    onClick = {},
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
            GroupCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                position = CardPosition.Single,
                onClick = {},
                title = state.tokenName,
                subtitle = String.format(Locale.US, "%+.2f%%", change),
                subtitleColor = changeColor,
                trailing = {
                    Text(
                        text = "${formatCrypto(state.balance)} ${state.tokenSymbol}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            // Extra info placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(147.dp)
                    .background(
                        color = Color(0xFF2B5E48),
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(TokenDetailsR.string.token_details_extra_info_placeholder),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChartPlaceholder(isLoading: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = stringResource(TokenDetailsR.string.token_details_chart_placeholder),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PriceChart(
    priceHistory: List<PricePoint>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (priceHistory.isEmpty()) {
        ChartPlaceholder(isLoading = isLoading, modifier = modifier.padding(horizontal = 16.dp))
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

private fun formatCrypto(amount: BigDecimal): String {
    val plain = amount.toPlainString()
    val dotIndex = plain.indexOf('.')
    return if (dotIndex == -1 || plain.length - dotIndex <= 7) plain
    else plain.substring(0, dotIndex + 7)
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
            ),
            onBackClick = {},
            onPeriodSelected = {},
            onReceiveClick = {},
        )
    }
}
