package com.yasashny.fortera.feature.createwallet.ui

import android.content.ClipData
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletIntent
import com.yasashny.fortera.feature.createwallet.presentation.CreateWalletState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.yasashny.fortera.core.ui.R as CoreR
import com.yasashny.fortera.feature.createwallet.R as CreateWalletR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateWalletLayout(
    state: CreateWalletState,
    onIntent: (CreateWalletIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val nameTemplate =
        stringResource(CreateWalletR.string.create_wallet_name_template)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(CreateWalletR.string.create_wallet_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CreateWalletIntent.BackClicked) }) {
                        Icon(
                            painter = painterResource(id = CoreR.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                CreateWalletR.string.create_wallet_back_cd
                            ),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is CreateWalletState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is CreateWalletState.ShowSeedPhrase -> {
                    ShowSeedPhraseContent(
                        state = state,
                        onCopyClick = {
                            scope.launch {
                                val clip = ClipData.newPlainText("", state.seedPhrase.joinToString(" "))
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    clip.description.extras = PersistableBundle().apply {
                                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                                    }
                                }
                                clipboard.setClipEntry(ClipEntry(clip))
                                delay(60_000)
                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", "")))
                            }
                        },
                        onCreateClick = { onIntent(CreateWalletIntent.CreateClicked(nameTemplate)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowSeedPhraseContent(
    state: CreateWalletState.ShowSeedPhrase,
    onCopyClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(CreateWalletR.string.create_wallet_phrase_warning),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SeedPhraseGrid(words = state.seedPhrase)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.clickable(onClick = onCopyClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(CreateWalletR.string.create_wallet_copy),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Icon(
                painter = painterResource(id = CoreR.drawable.ic_content_copy),
                contentDescription = stringResource(
                    CreateWalletR.string.create_wallet_copy_cd
                ),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCreateClick,
            enabled = !state.isCreating,
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            if (state.isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(
                        CreateWalletR.string.create_wallet_create_button
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SeedPhraseGrid(words: List<String>) {
    val leftColumn = words.filterIndexed { index, _ -> index % 2 == 0 }
    val rightColumn = words.filterIndexed { index, _ -> index % 2 == 1 }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            leftColumn.forEachIndexed { index, word ->
                val wordIndex = index * 2 + 1
                SeedWordCard(
                    index = wordIndex,
                    word = word,
                    shape = getCardShape(wordIndex, words.size)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rightColumn.forEachIndexed { index, word ->
                val wordIndex = index * 2 + 2
                SeedWordCard(
                    index = wordIndex,
                    word = word,
                    shape = getCardShape(wordIndex, words.size)
                )
            }
        }
    }
}

private fun getCardShape(index: Int, total: Int): RoundedCornerShape {
    val corner = 16.dp

    return when (index) {
        1 -> RoundedCornerShape(topStart = corner)
        2 -> RoundedCornerShape(topEnd = corner)
        total - 1 -> RoundedCornerShape(bottomStart = corner)
        total -> RoundedCornerShape(bottomEnd = corner)
        else -> RoundedCornerShape(4.dp)
    }
}

@Composable
private fun SeedWordCard(
    index: Int,
    word: String,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateWalletLayoutPreview() {
    val words = listOf(
        "abandon", "ability", "able", "about", "above", "absent",
        "absorb", "abstract", "absurd", "abuse", "access", "accident"
    )
    ForteraTheme {
        CreateWalletLayout(
            state = CreateWalletState.ShowSeedPhrase(seedPhrase = words),
            onIntent = {},
        )
    }
}
