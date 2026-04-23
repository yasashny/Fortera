package com.yasashny.fortera.feature.receive.receive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.toPath
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.TokenIcon
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.feature.receive.R as ReceiveR

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val QrExpressiveShape = GenericShape { size, _ ->
    val polygonPath = MaterialShapes.Ghostish.normalized().toPath().asComposePath()
    val matrix = Matrix()
    matrix.scale(size.width, size.height)
    polygonPath.transform(matrix)
    addPath(polygonPath)
}

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

@Composable
private fun chunkedAddress(address: String): AnnotatedString {
    val accent = MaterialTheme.colorScheme.primary
    val body = MaterialTheme.colorScheme.onSurface
    return buildAnnotatedString {
        if (address.isEmpty()) return@buildAnnotatedString
        val hasHexPrefix = address.startsWith("0x")
        val prefix = if (hasHexPrefix) "0x" else ""
        val rest = if (hasHexPrefix) address.drop(2) else address
        val groups = rest.chunked(4)
        val headCount = minOf(2, groups.size)
        val tailCount = minOf(2, (groups.size - headCount).coerceAtLeast(0))
        val midCount = (groups.size - headCount - tailCount).coerceAtLeast(0)

        if (prefix.isNotEmpty()) {
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                append(prefix)
            }
            append(' ')
        }
        val head = groups.take(headCount).joinToString(" ")
        val mid = groups.drop(headCount).take(midCount).joinToString(" ")
        val tail = groups.takeLast(tailCount).joinToString(" ")

        withStyle(SpanStyle(color = body)) {
            append(head)
            if (mid.isNotEmpty()) {
                append(' ')
                append(mid)
            }
        }
        if (tail.isNotEmpty()) {
            append(' ')
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                append(tail)
            }
        }
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            TokenIcon(
                iconUrl = tokenIconUrl(state.tokenSymbol),
                icon = CardIcon.Letter(state.tokenSymbol.firstOrNull() ?: '?'),
                badgeIconUrl = state.networkIconUrl,
                size = 56.dp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${state.tokenName} ${state.tokenSymbol}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(QrExpressiveShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                QrCodeDots(
                    content = state.address,
                    dotColor = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(180.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(
                        ReceiveR.string.receive_network_warning,
                        state.tokenSymbol,
                        state.networkName,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            ) {
                Text(
                    text = stringResource(ReceiveR.string.receive_wallet_address_label),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = onCopyAddress)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = chunkedAddress(state.address),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.25.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(ReceiveR.string.receive_copy_address),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(ReceiveR.string.receive_copy),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
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
