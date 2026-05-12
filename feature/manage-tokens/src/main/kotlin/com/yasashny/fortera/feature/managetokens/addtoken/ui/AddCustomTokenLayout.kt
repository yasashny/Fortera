package com.yasashny.fortera.feature.managetokens.addtoken.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.InputCard
import com.yasashny.fortera.core.ui.text.asString
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.feature.managetokens.addtoken.presentation.AddCustomTokenIntent
import com.yasashny.fortera.feature.managetokens.addtoken.presentation.AddCustomTokenState
import com.yasashny.fortera.feature.managetokens.addtoken.presentation.Verification
import kotlinx.coroutines.launch
import com.yasashny.fortera.core.ui.R as CoreR
import com.yasashny.fortera.feature.managetokens.R as ManageTokensR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddCustomTokenLayout(
    state: AddCustomTokenState,
    onIntent: (AddCustomTokenIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ManageTokensR.string.add_token_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(ManageTokensR.string.add_token_back_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            InputCard(
                value = state.input,
                onValueChange = { onIntent(AddCustomTokenIntent.InputChanged(it)) },
                placeholder = stringResource(ManageTokensR.string.add_token_input_placeholder),
                enabled = !state.isAdding,
                error = (state.verification as? Verification.Failed)?.message?.asString(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_content_paste),
                        contentDescription = stringResource(ManageTokensR.string.add_token_paste_cd),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(enabled = !state.isAdding) {
                                scope.launch {
                                    val pasted = clipboard.getClipEntry()
                                        ?.clipData
                                        ?.getItemAt(0)
                                        ?.text
                                        ?.toString()
                                        .orEmpty()
                                    if (pasted.isNotEmpty()) {
                                        onIntent(AddCustomTokenIntent.InputChanged(pasted))
                                    }
                                }
                            },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            Spacer(Modifier.height(12.dp))

            VerificationSlot(state = state)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onIntent(AddCustomTokenIntent.AddClicked) },
                enabled = state.canAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                if (state.isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(ManageTokensR.string.add_token_button),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VerificationSlot(state: AddCustomTokenState) {
    when (val verification = state.verification) {
        Verification.Idle, is Verification.Failed -> {
            HintText(text = stringResource(ManageTokensR.string.add_token_input_hint))
        }
        Verification.InProgress -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(ManageTokensR.string.add_token_verifying),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is Verification.Verified -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TokenPreviewCard(metadata = verification.metadata)
                if (verification.alreadyAdded) {
                    Text(
                        text = stringResource(ManageTokensR.string.add_token_error_already_added),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenPreviewCard(metadata: CustomTokenMetadata) {
    GroupCard(
        position = CardPosition.Single,
        onClick = {},
        title = metadata.name,
        titleSuffix = metadata.symbol,
        subtitle = stringResource(
            ManageTokensR.string.add_token_decimals_label,
            metadata.decimals,
        ),
        iconUrl = tokenIconUrl(metadata),
        icon = CardIcon.Letter(metadata.symbol.firstOrNull() ?: '?'),
        badgeIconUrl = tokenIconUrl("eth"),
    )
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun AddCustomTokenLayoutPreview() {
    ForteraTheme {
        AddCustomTokenLayout(
            state = AddCustomTokenState(
                input = "0x6B175474E89094C44Da98b954EedeAC495271d0F",
                verification = Verification.Verified(
                    metadata = CustomTokenMetadata(
                        name = "Dai Stablecoin",
                        symbol = "DAI",
                        decimals = 18,
                        contractAddress = "0x6b175474e89094c44da98b954eedeac495271d0f",
                        network = BlockchainNetwork.ETHEREUM,
                        coingeckoId = "dai",
                    ),
                    alreadyAdded = false,
                ),
            ),
            onIntent = {},
            onBackClick = {},
        )
    }
}
