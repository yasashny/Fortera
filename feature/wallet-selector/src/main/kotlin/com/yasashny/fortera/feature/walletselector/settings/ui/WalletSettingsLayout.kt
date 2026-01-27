package com.yasashny.fortera.feature.walletselector.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.InputCard
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract.Intent
import com.yasashny.fortera.feature.walletselector.settings.presentation.WalletSettingsContract.State
import com.yasashny.fortera.feature.walletselector.R as WalletSelectorR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalletSettingsLayout(
    state: State,
    onIntent: (Intent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(WalletSelectorR.string.wallet_settings_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(WalletSelectorR.string.wallet_settings_back_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                State.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                is State.Content -> {
                    WalletSettingsContent(
                        state = state,
                        onIntent = onIntent,
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (state.showDeleteDialog) {
                        DeleteWalletDialog(
                            onConfirm = { onIntent(Intent.ConfirmDelete) },
                            onDismiss = { onIntent(Intent.DismissDeleteDialog) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletSettingsContent(
    state: State.Content,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        InputCard(
            value = state.name,
            onValueChange = { onIntent(Intent.NameChanged(it)) },
            placeholder = stringResource(WalletSelectorR.string.wallet_settings_name_label),
            enabled = !state.isSaving,
            trailingIcon = {
                TextButton(
                    onClick = { onIntent(Intent.SaveClicked) },
                    enabled = state.name.isNotBlank() && !state.isSaving,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(20.dp),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(WalletSelectorR.string.wallet_settings_save),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onIntent(Intent.DeleteClicked) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(stringResource(WalletSelectorR.string.wallet_settings_delete))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


@Preview(showBackground = true)
@Composable
private fun WalletSettingsLayoutPreview() {
    ForteraTheme {
        WalletSettingsLayout(
            state = State.Content(
                walletId = "1",
                name = "Wallet №1",
                isSaving = false,
                showDeleteDialog = false,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
