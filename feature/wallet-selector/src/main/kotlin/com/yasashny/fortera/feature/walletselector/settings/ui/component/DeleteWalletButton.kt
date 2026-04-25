package com.yasashny.fortera.feature.walletselector.settings.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.walletselector.R

/**
 * Bottom-of-screen destructive action that opens [DeleteWalletDialog]. Disabled while a save
 * or delete is in flight to avoid double-submits.
 */
@Composable
internal fun DeleteWalletButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
    ) {
        Text(stringResource(R.string.wallet_settings_delete))
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteWalletButtonPreview() {
    ForteraTheme {
        DeleteWalletButton(
            onClick = {},
            enabled = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
