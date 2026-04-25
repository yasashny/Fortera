package com.yasashny.fortera.feature.walletselector.main.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.feature.walletselector.R

@Composable
internal fun WalletItem(
    wallet: Wallet,
    position: CardPosition,
    isActive: Boolean,
    onSelectClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GroupCard(
        position = position,
        onClick = onSelectClick,
        title = wallet.name,
        icon = CardIcon.Letter(wallet.name.firstOrNull()?.uppercaseChar() ?: 'W'),
        modifier = modifier,
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
                        contentDescription = stringResource(R.string.wallet_selector_settings_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WalletItemPreview() {
    ForteraTheme {
        WalletItem(
            wallet = Wallet(id = "1", name = "Wallet №1"),
            position = CardPosition.Single,
            isActive = true,
            onSelectClick = {},
            onSettingsClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
