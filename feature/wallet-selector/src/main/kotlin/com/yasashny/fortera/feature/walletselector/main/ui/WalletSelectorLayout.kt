package com.yasashny.fortera.feature.walletselector.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.ui.component.CardGroup
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.LazyCardGroup
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorContract.Intent
import com.yasashny.fortera.core.ui.R as CoreR
import com.yasashny.fortera.feature.walletselector.R as WalletSelectorR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalletSelectorLayout(
    state: WalletSelectorContract.State,
    onIntent: (Intent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = screenHeight / 2)) {
            WalletSelectorContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun WalletSelectorContent(
    state: WalletSelectorContract.State,
    onIntent: (Intent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
            GroupCard(
                position = CardPosition.First,
                onClick = { onIntent(Intent.CreateWalletClicked) },
                title = stringResource(WalletSelectorR.string.wallet_selector_create_new_wallet),
                icon = CardIcon.Resource(CoreR.drawable.ic_add),
            )
            GroupCard(
                position = CardPosition.Last,
                onClick = { onIntent(Intent.ImportWalletClicked) },
                title = stringResource(WalletSelectorR.string.wallet_selector_import_wallet),
                icon = CardIcon.Resource(CoreR.drawable.ic_download),
            )
        }

        if (state.wallets.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 60.dp))

            LazyCardGroup(
                items = state.wallets,
                modifier = Modifier.padding(horizontal = 16.dp),
                key = { it.id },
            ) { wallet, position ->
                WalletItem(
                    wallet = wallet,
                    position = position,
                    isActive = wallet.id == state.activeWalletId,
                    onSelectClick = { onIntent(Intent.SelectWallet(wallet.id)) },
                    onSettingsClick = { onIntent(Intent.SettingsClicked(wallet.id)) },
                )
            }
        }

        Spacer(modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun WalletItem(
    wallet: Wallet,
    position: CardPosition,
    isActive: Boolean,
    onSelectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    GroupCard(
        position = position,
        onClick = onSelectClick,
        title = wallet.name,
        icon = CardIcon.Letter(wallet.name.firstOrNull()?.uppercaseChar() ?: 'W'),
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(WalletSelectorR.string.wallet_selector_settings_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WalletSelectorLayoutPreview() {
    val wallets = listOf(
        Wallet(id = "1", name = "Wallet №1"),
        Wallet(id = "2", name = "Wallet №2"),
    )
    ForteraTheme {
        WalletSelectorLayout(
            state = WalletSelectorContract.State(
                wallets = wallets,
                activeWalletId = "1",
            ),
            onIntent = {},
            onDismiss = {},
        )
    }
}
