package com.yasashny.fortera.feature.walletselector.settings.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.InputCard
import com.yasashny.fortera.feature.walletselector.R

@Composable
internal fun WalletNameField(
    name: String,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    InputCard(
        value = name,
        onValueChange = onNameChange,
        placeholder = stringResource(R.string.wallet_settings_name_label),
        enabled = !isSaving,
        modifier = modifier,
        trailingIcon = {
            TextButton(
                onClick = onSaveClick,
                enabled = name.isNotBlank() && !isSaving,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(20.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.wallet_settings_save),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WalletNameFieldPreview() {
    ForteraTheme {
        WalletNameField(
            name = "Wallet №1",
            onNameChange = {},
            onSaveClick = {},
            isSaving = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}
