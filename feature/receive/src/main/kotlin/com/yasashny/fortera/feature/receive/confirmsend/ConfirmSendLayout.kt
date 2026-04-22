package com.yasashny.fortera.feature.receive.confirmsend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.TokenIcon
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import com.yasashny.fortera.feature.receive.R as ReceiveR

private fun shortAddress(address: String): String {
    if (address.length <= 12) return address
    val hasHexPrefix = address.startsWith("0x")
    val head = if (hasHexPrefix) address.take(6) else address.take(4)
    val tail = address.takeLast(4)
    return "$head…$tail"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfirmSendLayout(
    state: ConfirmSendContract.State,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onDismissSpeedSheet: () -> Unit,
    onSelectSpeed: (FeeSpeed) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(ReceiveR.string.send_confirm_title))
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
            Button(
                onClick = onSendClick,
                enabled = !state.isSending
                    && !state.isLoading
                    && state.commissions.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(59.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(ReceiveR.string.send_confirm_send),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            HeroCard(state = state)

            SummaryCard(
                state = state,
                onSpeedClick = onSpeedClick,
            )

            TotalCard(
                totalAmount = state.totalAmount,
                totalAmountUsd = state.totalAmountUsd,
            )

            WarningChip(networkName = state.networkName)

            Spacer(Modifier.height(4.dp))
        }
    }

    if (state.isSpeedSheetOpen) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = onDismissSpeedSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            SpeedSheetContent(
                commissions = state.commissions,
                selectedSpeed = state.selectedSpeed,
                onSelectSpeed = onSelectSpeed,
            )
        }
    }
}

@Composable
private fun HeroCard(state: ConfirmSendContract.State) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TokenIcon(
                    iconUrl = tokenIconUrl(state.tokenSymbol),
                    icon = CardIcon.Letter(state.tokenSymbol.firstOrNull() ?: '?'),
                    size = 44.dp,
                )
                Column {
                    Text(
                        text = "${state.tokenName} · ${state.tokenSymbol}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.6.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f),
                    )
                    Text(
                        text = stringResource(
                            ReceiveR.string.send_confirm_from_wallet,
                            state.walletName,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }

            Column {
                Text(
                    text = state.amount,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.inversePrimary,
                )
                if (state.amountUsd.isNotEmpty()) {
                    Text(
                        text = state.amountUsd,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    state: ConfirmSendContract.State,
    onSpeedClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp),
    ) {
        SummaryRow(label = stringResource(ReceiveR.string.send_confirm_to)) {
            RecipientChip(address = state.address)
        }
        SummaryDivider()
        SummaryRow(label = stringResource(ReceiveR.string.send_confirm_network)) {
            NetworkValue(networkName = state.networkName)
        }
        SummaryDivider()
        SummaryRow(
            label = stringResource(ReceiveR.string.send_confirm_fee),
            onClick = onSpeedClick,
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
        ) {
            FeeValue(state = state)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    value: @Composable () -> Unit,
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
        ) {
            value()
        }
        if (trailing != null) trailing()
    }
}

@Composable
private fun SummaryDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp,
    )
}

@Composable
private fun RecipientChip(address: String) {
    val short = shortAddress(address)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = address.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Text(
            text = buildAnnotatedString {
                if (short.startsWith("0x")) {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    ) { append("0x") }
                    append(short.drop(2))
                } else {
                    append(short)
                }
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.2.sp,
            ),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun NetworkValue(networkName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    shape = CircleShape,
                ),
        )
        Text(
            text = networkName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FeeValue(state: ConfirmSendContract.State) {
    val commission = state.commission
    Column(horizontalAlignment = Alignment.End) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpeedPill(speed = state.selectedSpeed, selected = true)
            Text(
                text = commission?.nativeAmount.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (!commission?.usdAmount.isNullOrEmpty()) {
            Text(
                text = commission.usdAmount,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpeedPill(
    speed: FeeSpeed,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when (speed) {
        FeeSpeed.SLOW ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        FeeSpeed.FAST ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        FeeSpeed.INSTANT ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    val label = stringResource(
        when (speed) {
            FeeSpeed.SLOW -> ReceiveR.string.send_confirm_speed_slow
            FeeSpeed.FAST -> ReceiveR.string.send_confirm_speed_fast
            FeeSpeed.INSTANT -> ReceiveR.string.send_confirm_speed_instant
        },
    )
    val icon = when (speed) {
        FeeSpeed.SLOW -> Icons.Default.HourglassBottom
        FeeSpeed.FAST -> Icons.Default.Speed
        FeeSpeed.INSTANT -> Icons.Default.Bolt
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) bg else Color.Transparent)
            .then(
                if (!selected) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(999.dp),
                ) else Modifier,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = if (selected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TotalCard(totalAmount: String, totalAmountUsd: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(ReceiveR.string.send_confirm_total).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.6.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (totalAmountUsd.isNotEmpty()) {
                Text(
                    text = totalAmountUsd,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = totalAmount,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WarningChip(networkName: String) {
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
            text = stringResource(ReceiveR.string.send_confirm_warning, networkName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun SpeedSheetContent(
    commissions: Map<FeeSpeed, ConfirmSendContract.CommissionInfo>,
    selectedSpeed: FeeSpeed,
    onSelectSpeed: (FeeSpeed) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(ReceiveR.string.send_confirm_speed_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(ReceiveR.string.send_confirm_speed_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        FeeSpeed.entries.forEach { speed ->
            SpeedOptionRow(
                speed = speed,
                commission = commissions[speed],
                selected = speed == selectedSpeed,
                onClick = { onSelectSpeed(speed) },
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SpeedOptionRow(
    speed: FeeSpeed,
    commission: ConfirmSendContract.CommissionInfo?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val labelRes = when (speed) {
        FeeSpeed.SLOW -> ReceiveR.string.send_confirm_speed_slow
        FeeSpeed.FAST -> ReceiveR.string.send_confirm_speed_fast
        FeeSpeed.INSTANT -> ReceiveR.string.send_confirm_speed_instant
    }
    val etaRes = when (speed) {
        FeeSpeed.SLOW -> ReceiveR.string.send_confirm_speed_slow_eta
        FeeSpeed.FAST -> ReceiveR.string.send_confirm_speed_fast_eta
        FeeSpeed.INSTANT -> ReceiveR.string.send_confirm_speed_instant_eta
    }
    val icon: ImageVector = when (speed) {
        FeeSpeed.SLOW -> Icons.Default.HourglassBottom
        FeeSpeed.FAST -> Icons.Default.Speed
        FeeSpeed.INSTANT -> Icons.Default.Bolt
    }

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(etaRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = commission?.nativeAmount.orEmpty(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = commission?.usdAmount.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmSendLayoutPreview() {
    ForteraTheme {
        ConfirmSendLayout(
            state = ConfirmSendContract.State(
                tokenName = "Ethereum",
                tokenSymbol = "ETH",
                walletName = "Wallet 1",
                amount = "0.000345 ETH",
                amountUsd = "≈ $0.86",
                address = "0xGGJ7GJHHJGJHFDFKJDFKFNKDBJF",
                networkName = "Ethereum",
                commissions = mapOf(
                    FeeSpeed.SLOW to ConfirmSendContract.CommissionInfo("0.000090 ETH", "≈ $0.09"),
                    FeeSpeed.FAST to ConfirmSendContract.CommissionInfo("0.000150 ETH", "≈ $0.15"),
                    FeeSpeed.INSTANT to ConfirmSendContract.CommissionInfo("0.000270 ETH", "≈ $0.27"),
                ),
                selectedSpeed = FeeSpeed.FAST,
                totalAmount = "0.000495 ETH",
                totalAmountUsd = "≈ $1.01",
                isLoading = false,
            ),
            onBackClick = {},
            onSendClick = {},
            onSpeedClick = {},
            onDismissSpeedSheet = {},
            onSelectSpeed = {},
        )
    }
}
