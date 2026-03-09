package com.yasashny.fortera.feature.receive.send

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.receive.R as ReceiveR
import java.math.BigDecimal

private fun tokenIconUrl(symbol: String): String =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${symbol.lowercase()}.png"

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
    val amountColor = if (state.insufficientFunds) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val buttonColors = if (state.insufficientFunds) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(ReceiveR.string.send_title))
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Amount input
            BasicTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    color = amountColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                )
                            }
                            innerTextField()
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = state.tokenSymbol,
                            style = MaterialTheme.typography.displayMedium,
                            color = amountColor,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        )
                    }
                },
            )

            // USD value
            if (state.amountUsd.isNotEmpty()) {
                Text(
                    text = "${state.amountUsd} $",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            // Address input with paste
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = state.address,
                    onValueChange = onAddressChange,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (state.address.isEmpty()) {
                                Text(
                                    text = stringResource(ReceiveR.string.send_input_address),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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

            // Error text
            if (state.insufficientFunds) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(ReceiveR.string.send_insufficient_funds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.weight(1f))

            // Wallet info at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                ) {
                    AsyncImage(
                        model = tokenIconUrl(state.tokenSymbol),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.tokenSymbol.firstOrNull()?.toString() ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Continue button
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                shape = RoundedCornerShape(20.dp),
                colors = buttonColors,
            ) {
                Text(
                    text = stringResource(ReceiveR.string.send_continue),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
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
                amountUsd = "0.86",
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
