package com.yasashny.fortera.feature.receive.receive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.feature.receive.R as ReceiveR

@Composable
private fun QrCodeDots(
    content: String,
    modifier: Modifier = Modifier,
    dotColor: androidx.compose.ui.graphics.Color,
) {
    val byteMatrix = remember(content) {
        if (content.isBlank()) null
        else runCatching {
            Encoder.encode(content, ErrorCorrectionLevel.H).matrix
        }.getOrNull()
    }
    Canvas(modifier = modifier) {
        val mtx = byteMatrix ?: return@Canvas
        val modules = mtx.width
        val cellSize = size.minDimension / modules
        val dotRadius = cellSize * 0.46f
        for (x in 0 until modules) {
            for (y in 0 until mtx.height) {
                if (mtx[x, y].toInt() == 1) {
                    val cx = x * cellSize + cellSize / 2f
                    val cy = y * cellSize + cellSize / 2f
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(cx, cy),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ReceiveLayout(
    state: ReceiveContract.State,
    onBackClick: () -> Unit,
    onCopyAddress: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ReceiveR.string.receive_title),
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            TokenStripCard(
                tokenName = state.tokenName,
                tokenSymbol = state.tokenSymbol,
                tokenIconUrl = state.tokenIconUrl,
                networkName = state.networkName,
                networkIconUrl = state.networkIconUrl,
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                QrCodeDots(
                    content = state.address,
                    dotColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            }

            Spacer(Modifier.height(18.dp))

            NetworkWarningCard(
                tokenSymbol = state.tokenSymbol,
                networkName = state.networkName,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(ReceiveR.string.receive_wallet_address_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(8.dp))

            AddressCard(
                address = state.address,
                onCopyAddress = onCopyAddress,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TokenStripCard(
    tokenName: String,
    tokenSymbol: String,
    tokenIconUrl: String,
    networkName: String,
    networkIconUrl: String?,
) {
    GroupCard(
        position = CardPosition.Single,
        onClick = {},
        title = tokenName,
        subtitle = networkName,
        iconUrl = tokenIconUrl,
        icon = CardIcon.Letter(tokenSymbol.firstOrNull() ?: '?'),
        badgeIconUrl = networkIconUrl,
        trailing = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = tokenSymbol,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }
        },
    )
}

@Composable
private fun NetworkWarningCard(
    tokenSymbol: String,
    networkName: String,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.TwoTone.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(
                ReceiveR.string.receive_network_warning,
                tokenSymbol,
                networkName,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddressCard(
    address: String,
    onCopyAddress: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        AddressChunks(address = address)

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onCopyAddress)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(ReceiveR.string.receive_copy_address),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddressChunks(address: String) {
    if (address.isEmpty()) return
    val hasHexPrefix = address.startsWith("0x")
    val rest = if (hasHexPrefix) address.drop(2) else address
    val groups = rest.chunked(4)

    val accent = MaterialTheme.colorScheme.primary
    val deep = MaterialTheme.colorScheme.inversePrimary
    val body = MaterialTheme.colorScheme.onSurface
    val chunkStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.8.sp,
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (hasHexPrefix) {
            Text(
                text = "0x",
                style = chunkStyle,
                color = accent,
                fontWeight = FontWeight.Medium,
            )
        }
        groups.forEachIndexed { index, chunk ->
            val color = when (index) {
                groups.lastIndex -> deep
                groups.lastIndex - 1 -> accent
                else -> body
            }
            val weight = if (index >= groups.lastIndex - 1) FontWeight.Medium else FontWeight.Normal
            Text(
                text = chunk,
                style = chunkStyle,
                color = color,
                fontWeight = weight,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveLayoutPreview() {
    ForteraTheme {
        ReceiveLayout(
            state = ReceiveContract.State(
                tokenName = "USD Coin",
                tokenSymbol = "USDC",
                networkName = "Ethereum · ERC-20",
                address = "0x27b47f0b3bb3f081e3f029a60424b8fd355dc4c2",
                isLoading = false,
            ),
            onBackClick = {},
            onCopyAddress = {},
        )
    }
}
