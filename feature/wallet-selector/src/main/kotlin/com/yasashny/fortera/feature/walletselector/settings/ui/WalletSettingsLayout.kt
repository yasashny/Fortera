package com.yasashny.fortera.feature.walletselector.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.walletselector.R
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsIntent
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsState
import com.yasashny.fortera.feature.walletselector.settings.ui.component.DeleteWalletButton
import com.yasashny.fortera.feature.walletselector.settings.ui.component.DeleteWalletDialog
import com.yasashny.fortera.feature.walletselector.settings.ui.component.WalletNameField
import com.yasashny.fortera.feature.walletselector.settings.ui.component.WalletSettingsShimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalletSettingsLayout(
    state: WalletSettingsState,
    onIntent: (WalletSettingsIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wallet_settings_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(WalletSettingsIntent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.wallet_settings_back_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            when (state) {
                WalletSettingsState.Loading -> WalletSettingsShimmer()
                is WalletSettingsState.Content -> WalletSettingsContent(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun WalletSettingsContent(
    state: WalletSettingsState.Content,
    onIntent: (WalletSettingsIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WalletNameField(
            name = state.name,
            onNameChange = { onIntent(WalletSettingsIntent.NameChanged(it)) },
            onSaveClick = { onIntent(WalletSettingsIntent.SaveClicked) },
            isSaving = state.isSaving,
        )

        Spacer(modifier = Modifier.weight(1f))

        DeleteWalletButton(
            onClick = { onIntent(WalletSettingsIntent.DeleteClicked) },
            enabled = !state.isSaving,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (state.showDeleteDialog) {
        DeleteWalletDialog(
            onConfirm = { onIntent(WalletSettingsIntent.ConfirmDelete) },
            onDismiss = { onIntent(WalletSettingsIntent.DismissDeleteDialog) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WalletSettingsLayoutPreview() {
    ForteraTheme {
        WalletSettingsLayout(
            state = WalletSettingsState.Content(
                walletId = "1",
                name = "Wallet №1",
            ),
            onIntent = {},
        )
    }
}
