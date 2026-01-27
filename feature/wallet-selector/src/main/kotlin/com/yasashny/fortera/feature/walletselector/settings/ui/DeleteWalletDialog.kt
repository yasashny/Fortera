package com.yasashny.fortera.feature.walletselector.settings.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yasashny.fortera.feature.walletselector.R as WalletSelectorR

@Composable
internal fun DeleteWalletDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(WalletSelectorR.string.wallet_settings_delete_dialog_title)) },
        text = { Text(stringResource(WalletSelectorR.string.wallet_settings_delete_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(WalletSelectorR.string.wallet_settings_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(WalletSelectorR.string.wallet_settings_delete_dialog_cancel))
            }
        },
    )
}