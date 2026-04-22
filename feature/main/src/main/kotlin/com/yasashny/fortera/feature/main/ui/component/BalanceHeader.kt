package com.yasashny.fortera.feature.main.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.ui.component.ShimmerBox
import com.yasashny.fortera.feature.main.R
import com.yasashny.fortera.feature.main.presentation.BalancesState
import com.yasashny.fortera.core.ui.format.formatUsd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollapsedBalanceBar(
    walletName: String?,
    balances: BalancesState,
    staleAlpha: Float,
) {
    TopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.Start) {
                when (walletName) {
                    null -> ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 18.dp))
                    else -> Text(
                        text = walletName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                when (balances) {
                    BalancesState.Loading -> ShimmerBox(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(width = 70.dp, height = 14.dp),
                    )
                    is BalancesState.Ready -> Text(
                        text = formatUsd(balances.totalUsd),
                        modifier = Modifier.alpha(staleAlpha),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
    )
}

@Composable
internal fun ExpandedBalanceHeader(
    walletName: String?,
    balances: BalancesState,
    staleAlpha: Float,
    onWalletSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
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
                when (walletName) {
                    null -> ShimmerBox(
                        modifier = Modifier.size(width = 120.dp, height = 28.dp),
                    )
                    else -> {
                        Text(
                            text = walletName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.main_settings_cd),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        when (balances) {
            BalancesState.Loading -> Box(
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
            is BalancesState.Ready -> Text(
                text = formatUsd(balances.totalUsd),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, bottom = 60.dp)
                    .alpha(staleAlpha),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
