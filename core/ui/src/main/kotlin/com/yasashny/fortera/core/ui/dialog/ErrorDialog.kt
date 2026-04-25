package com.yasashny.fortera.core.ui.dialog

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.ui.R
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.core.ui.text.asString

@Composable
fun ErrorDialog(
    message: UiText?,
    onDismiss: () -> Unit,
    title: UiText? = null,
    confirmLabel: UiText? = null,
) {
    if (message == null) return

    val resolvedTitle = title?.asString() ?: stringResource(R.string.core_ui_error_title)
    val resolvedConfirm = confirmLabel?.asString() ?: stringResource(R.string.core_ui_ok)
    val resolvedMessage = message.asString()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp),
            )
        },
        title = {
            Text(
                text = resolvedTitle,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = resolvedMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = resolvedConfirm)
            }
        },
    )
}
