package com.yasashny.fortera.feature.walletselector.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.ui.component.CardGroup
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorIntent
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletSelectorState
import com.yasashny.fortera.feature.walletselector.main.presentation.WalletsState
import com.yasashny.fortera.feature.walletselector.main.ui.component.CreateImportActions
import com.yasashny.fortera.feature.walletselector.main.ui.component.WalletItem
import com.yasashny.fortera.feature.walletselector.main.ui.component.WalletListShimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalletSelectorLayout(
    state: WalletSelectorState,
    onIntent: (WalletSelectorIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerSize = LocalWindowInfo.current.containerSize
    val minSheetHeight = with(LocalDensity.current) { containerSize.height.toDp() } / 2

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        WalletSelectorContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.heightIn(min = minSheetHeight),
        )
    }
}

@Composable
private fun WalletSelectorContent(
    state: WalletSelectorState,
    onIntent: (WalletSelectorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        CreateImportActions(
            onCreateClick = { onIntent(WalletSelectorIntent.CreateWalletClicked) },
            onImportClick = { onIntent(WalletSelectorIntent.ImportWalletClicked) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        WalletListSection(
            wallets = state.wallets,
            onIntent = onIntent,
        )

        Spacer(modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun WalletListSection(
    wallets: WalletsState,
    onIntent: (WalletSelectorIntent) -> Unit,
) {
    val sectionPadding = Modifier.padding(horizontal = 16.dp)

    when (wallets) {
        WalletsState.Loading -> {
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 60.dp))
            WalletListShimmer(modifier = sectionPadding)
        }
        is WalletsState.Ready -> {
            if (wallets.items.isEmpty()) return
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 60.dp))
            CardGroup(
                items = wallets.items,
                modifier = sectionPadding,
                key = { it.id },
            ) { wallet, position ->
                WalletItem(
                    wallet = wallet,
                    position = position,
                    isActive = wallet.id == wallets.activeWalletId,
                    onSelectClick = { onIntent(WalletSelectorIntent.SelectWallet(wallet.id)) },
                    onSettingsClick = { onIntent(WalletSelectorIntent.SettingsClicked(wallet.id)) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WalletSelectorLayoutPreview() {
    ForteraTheme {
        WalletSelectorContent(
            state = WalletSelectorState(
                wallets = WalletsState.Ready(
                    items = listOf(
                        Wallet(id = "1", name = "Wallet №1"),
                        Wallet(id = "2", name = "Wallet №2"),
                    ),
                    activeWalletId = "1",
                ),
            ),
            onIntent = {},
        )
    }
}
