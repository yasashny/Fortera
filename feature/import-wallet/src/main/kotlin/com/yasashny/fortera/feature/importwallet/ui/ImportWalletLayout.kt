package com.yasashny.fortera.feature.importwallet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.InputCard
import com.yasashny.fortera.core.ui.component.InputCardGroup
import com.yasashny.fortera.core.ui.text.asString
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletIntent
import com.yasashny.fortera.feature.importwallet.presentation.ImportWalletState
import kotlinx.coroutines.launch
import com.yasashny.fortera.core.ui.R as CoreR
import com.yasashny.fortera.feature.importwallet.R as ImportWalletR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportWalletLayout(
    state: ImportWalletState,
    onIntent: (ImportWalletIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ImportWalletR.string.import_wallet_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(ImportWalletIntent.BackClicked) }) {
                        Icon(
                            painter = painterResource(id = CoreR.drawable.ic_arrow_back),
                            contentDescription = stringResource(ImportWalletR.string.import_wallet_back_cd),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        when (state) {
            is ImportWalletState.Content -> {
                ImportWalletContent(
                    state = state,
                    onNameChanged = { onIntent(ImportWalletIntent.NameChanged(it)) },
                    onSeedPhraseChanged = { onIntent(ImportWalletIntent.SeedPhraseChanged(it)) },
                    onPasteClick = {
                        scope.launch {
                            val clip = clipboard.getClipEntry()
                            val pastedText = clip?.clipData?.getItemAt(0)?.text?.toString() ?: ""
                            onIntent(ImportWalletIntent.SeedPhraseChanged(pastedText))
                        }
                    },
                    onImportClick = { onIntent(ImportWalletIntent.ImportClicked) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ImportWalletContent(
    state: ImportWalletState.Content,
    onNameChanged: (String) -> Unit,
    onSeedPhraseChanged: (String) -> Unit,
    onPasteClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        InputCardGroup {
            InputCard(
                value = state.name,
                onValueChange = onNameChanged,
                placeholder = stringResource(ImportWalletR.string.import_wallet_name_placeholder),
                enabled = !state.isLoading,
                error = state.nameError?.asString(),
                position = CardPosition.First,
            )
            InputCard(
                value = state.seedPhrase,
                onValueChange = onSeedPhraseChanged,
                placeholder = stringResource(ImportWalletR.string.import_wallet_seed_phrase_placeholder),
                enabled = !state.isLoading,
                error = state.seedPhraseError?.asString(),
                position = CardPosition.Last,
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_content_paste),
                        contentDescription = stringResource(ImportWalletR.string.import_wallet_paste_cd),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onPasteClick),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = !state.isLoading && state.name.isNotBlank() && state.seedPhrase.isNotBlank()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(ImportWalletR.string.import_wallet_import_button),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportWalletLayoutPreview() {
    ForteraTheme {
        ImportWalletLayout(
            state = ImportWalletState.Content(
                name = "",
                seedPhrase = "",
            ),
            onIntent = {},
        )
    }
}