package com.yasashny.fortera.feature.managetokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.cryptoapi.TokenCatalog
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.TokenItem
import com.yasashny.fortera.feature.managetokens.R as ManageTokensR
import java.util.Locale

private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageTokensLayout(
    state: ManageTokensContract.State,
    onBackClick: () -> Unit,
    onToggle: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(ManageTokensR.string.manage_tokens_title))
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
        LazyColumn(contentPadding = paddingValues) {
            items(state.tokens, key = { it.token.id }) { item ->
                TokenRow(item = item, onToggle = onToggle)
            }
        }
    }
}

@Composable
private fun TokenRow(
    item: TokenItem,
    onToggle: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.token.symbol.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // Name + change%
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.token.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                val change = item.changePercent24h
                if (item.priceUsd > 0.0) {
                    Text(
                        text = String.format(Locale.US, "%+.2f%%", change),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (change >= 0) ChangePositive else ChangeNegative,
                    )
                }
            }

            // Price + switch
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (item.priceUsd > 0.0) {
                    Text(
                        text = formatPrice(item.priceUsd),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                    )
                }
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = { onToggle(item.token.id) },
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String = when {
    price >= 1_000 -> String.format(Locale.US, "$%.0f", price)
    price >= 1 -> String.format(Locale.US, "$%.2f", price)
    else -> String.format(Locale.US, "$%.4f", price)
}

@Preview(showBackground = true)
@Composable
private fun ManageTokensLayoutPreview() {
    ForteraTheme {
        ManageTokensLayout(
            state = ManageTokensContract.State(
                tokens = TokenCatalog.tokens.map { token ->
                    TokenItem(
                        token = token,
                        isEnabled = token.isDefault,
                        priceUsd = if (token.id == "bitcoin") 65000.0 else if (token.id == "ethereum") 3200.0 else 1.0,
                        changePercent24h = if (token.id == "bitcoin") -0.02 else 0.02,
                    )
                },
                isLoading = false,
            ),
            onBackClick = {},
            onToggle = {},
        )
    }
}
