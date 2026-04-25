package com.yasashny.fortera.feature.receive.send

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.ShimmerBox
import com.yasashny.fortera.core.ui.component.TokenIcon
import com.yasashny.fortera.core.ui.currency.LocalFiat
import com.yasashny.fortera.core.ui.format.formatFiat
import com.yasashny.fortera.core.ui.text.asString
import com.yasashny.fortera.feature.receive.R as ReceiveR
import java.math.BigDecimal

private fun formatBalance(balance: BigDecimal, symbol: String): String {
    val plain = balance.toPlainString()
    val dotIndex = plain.indexOf('.')
    val formatted = if (dotIndex == -1 || plain.length - dotIndex <= 8) plain
    else plain.substring(0, dotIndex + 8)
    return "$formatted $symbol"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SendLayout(
    state: SendContract.State,
    onBackClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    val amountColor = if (state.insufficientFunds || state.amountError != null) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val addressColor = if (state.addressError != null) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ReceiveR.string.send_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.ime.union(WindowInsets.navigationBars),
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TokenIcon(
                        iconUrl = state.tokenIconUrl,
                        icon = CardIcon.Letter(state.tokenSymbol.firstOrNull() ?: '?'),
                        size = 40.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = formatBalance(state.balance, state.tokenSymbol),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = state.tokenName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Button(
                    onClick = onContinueClick,
                    enabled = state.canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        text = stringResource(ReceiveR.string.send_continue),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            BasicTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    color = amountColor,
                    fontWeight = FontWeight.Medium,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(amountColor),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.Bottom) {
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            if (state.amount.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = amountColor.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            innerTextField()
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = state.tokenSymbol,
                            style = MaterialTheme.typography.displayMedium,
                            color = amountColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
            )

            state.amountUsd?.let { amountUsd ->
                val fiatText = formatFiat(amountUsd, LocalFiat.current)
                if (fiatText != null) {
                    Text(
                        text = fiatText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 16.dp))
                }
            }

            val amountHint: String? = when {
                state.amountError != null -> state.amountError.asString()
                state.insufficientFunds -> stringResource(ReceiveR.string.send_insufficient_funds)
                else -> null
            }
            if (amountHint != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = amountHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            val addressStyle = MaterialTheme.typography.titleMedium
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = state.address,
                    onValueChange = onAddressChange,
                    textStyle = addressStyle.copy(
                        color = addressColor,
                        fontWeight = FontWeight.Medium,
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    cursorBrush = SolidColor(addressColor),
                    decorationBox = { innerTextField ->
                        Box {
                            if (state.address.isEmpty()) {
                                Text(
                                    text = stringResource(ReceiveR.string.send_input_address),
                                    style = addressStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                IconButton(onClick = onPasteClick) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = stringResource(ReceiveR.string.send_paste),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.addressError != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.addressError.asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SendLayoutPreview() {
    ForteraTheme {
        SendLayout(
            state = SendContract.State(
                tokenId = "ethereum",
                tokenName = "Ethereum",
                tokenSymbol = "ETH",
                balance = BigDecimal("0.1949040"),
                priceUsd = 2500.0,
                amount = "0.000345",
                amountUsd = 0.86,
                isLoading = false,
            ),
            onBackClick = {},
            onAmountChange = {},
            onAddressChange = {},
            onPasteClick = {},
            onContinueClick = {},
        )
    }
}
