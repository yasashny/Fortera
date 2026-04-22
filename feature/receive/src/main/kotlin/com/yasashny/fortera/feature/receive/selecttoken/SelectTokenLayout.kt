package com.yasashny.fortera.feature.receive.selecttoken

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.domaincrypto.TokenCatalog
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.cardGroupItems
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.feature.receive.R as ReceiveR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectTokenLayout(
    title: String,
    state: SelectTokenContract.State,
    onBackClick: () -> Unit,
    onTokenClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = title)
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
                key = { it.id },
            ) { token, position ->
                val subtitle = if (token.contractAddress != null) {
                    token.network.displayName
                } else {
                    null
                }
                val badgeUrl = if (token.contractAddress != null) {
                    tokenIconUrl("eth")
                } else {
                    null
                }
                GroupCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    position = position,
                    onClick = { onTokenClick(token.id) },
                    title = token.name,
                    titleSuffix = token.symbol,
                    subtitle = subtitle,
                    iconUrl = tokenIconUrl(token.symbol),
                    icon = CardIcon.Letter(token.symbol.first()),
                    badgeIconUrl = badgeUrl,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectTokenLayoutPreview() {
    ForteraTheme {
        SelectTokenLayout(
            title = "Receive",
            state = SelectTokenContract.State(
                tokens = TokenCatalog.tokens,
                isLoading = false,
            ),
            onBackClick = {},
            onTokenClick = {},
        )
    }
}
