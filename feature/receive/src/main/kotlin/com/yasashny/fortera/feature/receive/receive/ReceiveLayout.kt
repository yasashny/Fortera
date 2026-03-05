package com.yasashny.fortera.feature.receive.receive

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.receive.R as ReceiveR

private fun tokenIconUrl(symbol: String): String =
    "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${symbol.lowercase()}.png"

@Composable
private fun rememberQrBitmap(
    content: String,
    sizePx: Int,
    foregroundColor: Int,
    backgroundColor: Int,
): Bitmap? {
    return remember(content, sizePx, foregroundColor, backgroundColor) {
        if (content.isBlank()) return@remember null
        runCatching {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) foregroundColor else backgroundColor)
                }
            }
            bitmap
        }.getOrNull()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                    Text(text = stringResource(ReceiveR.string.receive_title))
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // Token icon + network badge
            Box(modifier = Modifier.size(48.dp)) {
                AsyncImage(
                    model = tokenIconUrl(state.tokenSymbol),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                if (state.networkIconUrl != null) {
                    AsyncImage(
                        model = state.networkIconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape,
                            )
                            .padding(1.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${state.tokenName} ${state.tokenSymbol}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(32.dp))

            // QR code
            val qrForeground = MaterialTheme.colorScheme.onPrimary.toArgb()
            val qrBackground = MaterialTheme.colorScheme.primary.toArgb()
            val qrBitmap = rememberQrBitmap(
                content = state.address,
                sizePx = 512,
                foregroundColor = qrForeground,
                backgroundColor = qrBackground,
            )
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Address card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                onClick = onCopyAddress,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = state.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(ReceiveR.string.receive_copy_address),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Network warning
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        ReceiveR.string.receive_network_warning,
                        state.tokenSymbol,
                        state.networkName,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveLayoutPreview() {
    ForteraTheme {
        ReceiveLayout(
            state = ReceiveContract.State(
                tokenName = "Ethereum",
                tokenSymbol = "ETH",
                networkName = "Ethereum (ERC-20)",
                address = "0x1234567890abcdef1234567890abcdef12345678",
                isLoading = false,
            ),
            onBackClick = {},
            onCopyAddress = {},
        )
    }
}
