package com.yasashny.fortera.feature.managetokens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.cardGroupItems
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.feature.managetokens.ManageTokensContract.TokenItem
import com.yasashny.fortera.feature.managetokens.R as ManageTokensR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageTokensLayout(
    state: ManageTokensContract.State,
    onBackClick: () -> Unit,
    onToggle: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ManageTokensR.string.manage_tokens_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            cardGroupItems(
                items = state.tokens,
                key = { it.token.id },
            ) { item, position ->
                val subtitle = if (item.token.contractAddress != null) {
                    item.token.network.displayName
                } else {
                    null
                }
                val badgeUrl = if (item.token.contractAddress != null) {
                    tokenIconUrl("eth")
                } else {
                    null
                }
                GroupCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    position = position,
                    onClick = { onToggle(item.token.id) },
                    title = item.token.name,
                    titleSuffix = item.token.symbol,
                    subtitle = subtitle,
                    iconUrl = tokenIconUrl(item.token.symbol),
                    icon = CardIcon.Letter(item.token.symbol.first()),
                    badgeIconUrl = badgeUrl,
                    trailing = {
                        Switch(
                            checked = item.isEnabled,
                            onCheckedChange = { onToggle(item.token.id) },
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageTokensLayoutPreview() {
    ForteraTheme {
        ManageTokensLayout(
            state = ManageTokensContract.State(
                tokens = TokenCatalog.tokens.map { token ->
                    TokenItem(token = token, isEnabled = token.isDefault)
                },
                isLoading = false,
            ),
            onBackClick = {},
            onToggle = {},
        )
    }
}
