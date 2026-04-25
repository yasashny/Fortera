package com.yasashny.fortera.feature.walletselector.settings.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.walletselector.R

/**
 * Destructive confirmation for wallet deletion. The dialog is rendered conditionally off
 * [WalletSettingsContract.State.Content.showDeleteDialog] — pass `onConfirm` to commit the
 * delete and `onDismiss` for everything else (outside-tap, cancel, back-press).
 */
@Composable
internal fun DeleteWalletDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wallet_settings_delete_dialog_title)) },
        text = { Text(stringResource(R.string.wallet_settings_delete_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.wallet_settings_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.wallet_settings_delete_dialog_cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun DeleteWalletDialogPreview() {
    ForteraTheme {
        DeleteWalletDialog(onConfirm = {}, onDismiss = {})
    }
}
