package com.yasashny.fortera.feature.receive.confirmsend.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.twotone.Warning
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasashny.fortera.core.common.Haptics
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardGroup
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.ShimmerBox
import com.yasashny.fortera.core.ui.component.TokenIcon
import com.yasashny.fortera.core.ui.component.cardShapeForPosition
import com.yasashny.fortera.core.ui.currency.FiatDisplay
import com.yasashny.fortera.core.ui.currency.LocalFiat
import com.yasashny.fortera.core.ui.format.formatFiat
import com.yasashny.fortera.core.domaincrypto.model.FeeEstimate
import com.yasashny.fortera.core.domaincrypto.model.FeeSpeed
import java.math.BigDecimal
import com.yasashny.fortera.feature.receive.R as ReceiveR
import com.yasashny.fortera.feature.receive.confirmsend.presentation.ConfirmSendIntent
import com.yasashny.fortera.feature.receive.confirmsend.presentation.ConfirmSendState
import com.yasashny.fortera.feature.receive.confirmsend.presentation.CommissionInfo

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
    state: ConfirmSendState,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onDismissSpeedSheet: () -> Unit,
    onSelectSpeed: (FeeSpeed) -> Unit,
    onAddressClick: () -> Unit,
    onDismissAddressSheet: () -> Unit,
    onCopyAddress: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ReceiveR.string.send_confirm_title),
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
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                SlideToConfirmButton(
                    text = stringResource(ReceiveR.string.send_confirm_slide_to_confirm),
                    enabled = !state.isLoading
                        && state.commissions.isNotEmpty()
                        && !state.insufficientGas
                        && !state.isSending,
                    isSending = state.isSending,
                    onConfirm = onSendClick,
                )
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
                onAddressClick = onAddressClick,
            )

            TotalCard(
                totalAmount = state.totalAmount,
                totalAmountUsd = state.totalAmountUsd,
                fiat = LocalFiat.current,
            )

            if (state.insufficientGas) {
                InsufficientGasBanner(
                    feeSymbol = state.commission?.estimate?.nativeSymbol ?: "",
                )
            }

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

    if (state.isAddressSheetOpen) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = onDismissAddressSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            AddressSheetContent(
                state = state,
                onCopyAddress = onCopyAddress,
            )
        }
    }
}

@Composable
private fun HeroCard(state: ConfirmSendState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isLoading) {
                ShimmerBox(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                )
            } else {
                TokenIcon(
                    iconUrl = state.tokenIconUrl,
                    icon = CardIcon.Letter(state.tokenSymbol.firstOrNull() ?: '?'),
                    size = 44.dp,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state.isLoading) {
                    ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 12.dp))
                    ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 16.dp))
                } else {
                    Text(
                        text = "${state.tokenName} · ${state.tokenSymbol}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.6.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    val displayWalletName = state.walletName.ifBlank {
                        stringResource(ReceiveR.string.send_confirm_default_wallet_name)
                    }
                    Text(
                        text = stringResource(
                            ReceiveR.string.send_confirm_from_wallet,
                            displayWalletName,
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (state.isLoading) {
                ShimmerBox(modifier = Modifier.size(width = 200.dp, height = 36.dp))
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 16.dp))
            } else {
                Text(
                    text = state.amount,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                state.amountUsd?.let { usd ->
                    val fiatText = formatFiat(usd, LocalFiat.current, approximate = true)
                    if (fiatText != null) {
                        Text(
                            text = fiatText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    state: ConfirmSendState,
    onSpeedClick: () -> Unit,
    onAddressClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp),
    ) {
        SummaryRow(
            label = stringResource(ReceiveR.string.send_confirm_to),
            onClick = onAddressClick.takeUnless { state.isLoading },
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
        ) {
            if (state.isLoading) {
                ShimmerBox(
                    modifier = Modifier.size(width = 140.dp, height = 28.dp),
                    shape = RoundedCornerShape(999.dp),
                )
            } else {
                RecipientChip(address = state.address)
            }
        }
        SummaryDivider()
        SummaryRow(label = stringResource(ReceiveR.string.send_confirm_network)) {
            if (state.isLoading) {
                ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 16.dp))
            } else {
                NetworkValue(networkName = state.networkName)
            }
        }
        SummaryDivider()
        SummaryRow(
            label = stringResource(ReceiveR.string.send_confirm_fee),
            onClick = onSpeedClick.takeUnless { state.isFeesLoading },
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
        ) {
            if (state.isFeesLoading && state.commission == null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 16.dp))
                    ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 12.dp))
                }
            } else {
                FeeValue(state = state)
            }
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
private fun FeeValue(state: ConfirmSendState) {
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
        if (commission != null) {
            val fiatText = formatFiat(commission.feeUsd, LocalFiat.current, approximate = true)
            if (fiatText != null) {
                Text(
                    text = fiatText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 12.dp))
            }
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
private fun TotalCard(
    totalAmount: String,
    totalAmountUsd: Double?,
    fiat: FiatDisplay,
) {
    val loading = totalAmount.isEmpty()
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(ReceiveR.string.send_confirm_total).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.6.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (loading) {
                ShimmerBox(modifier = Modifier.size(width = 70.dp, height = 12.dp))
            } else if (totalAmountUsd != null) {
                val fiatText = formatFiat(totalAmountUsd, fiat, approximate = true)
                if (fiatText != null) {
                    Text(
                        text = fiatText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    ShimmerBox(modifier = Modifier.size(width = 70.dp, height = 12.dp))
                }
            }
        }
        if (loading) {
            ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 22.dp))
        } else {
            Text(
                text = totalAmount,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
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
private fun InsufficientGasBanner(feeSymbol: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.TwoTone.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(ReceiveR.string.send_confirm_insufficient_gas, feeSymbol),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun SpeedSheetContent(
    commissions: Map<FeeSpeed, CommissionInfo>,
    selectedSpeed: FeeSpeed,
    onSelectSpeed: (FeeSpeed) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
        }

        CardGroup(items = FeeSpeed.entries) { speed, position ->
            val commission = commissions[speed]
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
            val selected = speed == selectedSpeed

            val fiat = LocalFiat.current
            GroupCard(
                position = position,
                onClick = { onSelectSpeed(speed) },
                title = stringResource(labelRes),
                subtitle = stringResource(etaRes),
                icon = CardIcon.Vector(icon),
                modifier = if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = cardShapeForPosition(position),
                    )
                } else {
                    Modifier
                },
                trailing = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = commission?.nativeAmount.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        val fiatText = commission?.feeUsd
                            ?.let { formatFiat(it, fiat, approximate = true) }
                        if (fiatText != null) {
                            Text(
                                text = fiatText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else if (commission != null) {
                            ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 12.dp))
                        }
                    }
                },
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SlideToConfirmButton(
    text: String,
    enabled: Boolean,
    isSending: Boolean,
    onConfirm: () -> Unit,
) {
    val density = LocalDensity.current
    val haptics = koinInject<Haptics>()
    val scope = rememberCoroutineScope()

    val trackHeight = 59.dp
    val trackPadding = 5.dp
    val thumbSize = trackHeight - trackPadding * 2

    val trackPaddingPx = with(density) { trackPadding.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }

    var trackWidthPx by remember { mutableIntStateOf(0) }
    val offset = remember { Animatable(0f) }

    val maxOffsetPx = (trackWidthPx - thumbSizePx - trackPaddingPx * 2).coerceAtLeast(0f)

    LaunchedEffect(isSending, maxOffsetPx) {
        if (maxOffsetPx <= 0f) return@LaunchedEffect
        val target = if (isSending) maxOffsetPx else 0f
        if (offset.value != target) offset.animateTo(target, spring())
    }

    val isDisabled = !enabled && !isSending
    val disabledContainer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    val trackBg = if (isDisabled) disabledContainer else MaterialTheme.colorScheme.onPrimary
    val thumbBg = if (isDisabled) disabledContainer else MaterialTheme.colorScheme.primary
    val thumbContent = if (isDisabled) disabledContent else MaterialTheme.colorScheme.onPrimary
    val hintColor = if (isDisabled) disabledContent else MaterialTheme.colorScheme.primary
    val fillTint = if (isDisabled) disabledContent else MaterialTheme.colorScheme.primary

    val progress = if (maxOffsetPx > 0f) (offset.value / maxOffsetPx).coerceIn(0f, 1f) else 0f
    val fillBrush = Brush.horizontalGradient(
        0f to fillTint.copy(alpha = 0.18f),
        1f to fillTint.copy(alpha = 0f),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(trackBg)
            .onSizeChanged { trackWidthPx = it.width },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(
                    with(density) {
                        (offset.value + thumbSizePx + trackPaddingPx * 2).toDp()
                    },
                )
                .background(fillBrush),
        )

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = hintColor.copy(alpha = 1f - progress * 0.8f),
            modifier = Modifier.align(Alignment.Center),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = (offset.value + trackPaddingPx).roundToInt(),
                        y = 0,
                    )
                }
                .size(thumbSize)
                .clip(RoundedCornerShape(16.dp))
                .background(thumbBg)
                .pointerInput(enabled, maxOffsetPx) {
                    if (!enabled || maxOffsetPx <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offset.value >= maxOffsetPx * 0.95f) {
                                    offset.animateTo(maxOffsetPx, spring())
                                    haptics.click()
                                    onConfirm()
                                } else {
                                    offset.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offset.animateTo(0f, spring()) }
                        },
                        onHorizontalDrag = { _, delta ->
                            scope.launch {
                                offset.snapTo(
                                    (offset.value + delta).coerceIn(0f, maxOffsetPx),
                                )
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = thumbContent,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = thumbContent,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun AddressSheetContent(
    state: ConfirmSendState,
    onCopyAddress: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable(onClick = onCopyAddress)
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = fullAddress(state.address),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(ReceiveR.string.receive_copy),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun fullAddress(address: String) = buildAnnotatedString {
    if (address.startsWith("0x")) {
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            ),
        ) { append("0x") }
        append(address.drop(2))
    } else {
        append(address)
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmSendLayoutPreview() {
    ForteraTheme {
        ConfirmSendLayout(
            state = ConfirmSendState(
                tokenName = "Ethereum",
                tokenSymbol = "ETH",
                walletName = "Wallet 1",
                amount = "0.000345 ETH",
                amountUsd = 0.86,
                address = "0xGGJ7GJHHJGJHFDFKJDFKFNKDBJF",
                networkName = "Ethereum",
                commissions = mapOf(
                    FeeSpeed.SLOW to previewCommission("0.000090", 0.09),
                    FeeSpeed.FAST to previewCommission("0.000150", 0.15),
                    FeeSpeed.INSTANT to previewCommission("0.000270", 0.27),
                ),
                selectedSpeed = FeeSpeed.FAST,
                totalAmount = "0.000495 ETH",
                totalAmountUsd = 1.01,
                isLoading = false,
            ),
            onBackClick = {},
            onSendClick = {},
            onSpeedClick = {},
            onDismissSpeedSheet = {},
            onSelectSpeed = {},
            onAddressClick = {},
            onDismissAddressSheet = {},
            onCopyAddress = {},
        )
    }
}

private fun previewCommission(
    nativeAmount: String,
    usd: Double,
): CommissionInfo {
    val decimal = BigDecimal(nativeAmount)
    return CommissionInfo(
        estimate = FeeEstimate(nativeAmount = decimal, nativeSymbol = "ETH"),
        nativeAmount = "$nativeAmount ETH",
        feeUsd = usd,
    )
}
