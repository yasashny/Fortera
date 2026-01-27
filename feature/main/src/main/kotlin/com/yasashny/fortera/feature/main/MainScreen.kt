package com.yasashny.fortera.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.domain.wallet.Wallet
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.component.cardPosition
import com.yasashny.fortera.feature.createwallet.CreateWallet
import com.yasashny.fortera.feature.importwallet.ImportWallet
import com.yasashny.fortera.feature.walletselector.main.ui.WalletSelectorSheet
import com.yasashny.fortera.feature.main.MainContract.Effect
import com.yasashny.fortera.feature.main.MainContract.Intent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val navigator = LocalAppNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current
    var showWalletSelector by remember { mutableStateOf(false) }

    if (showWalletSelector) {
        WalletSelectorSheet(onDismiss = { showWalletSelector = false })
    }

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateToCreateWallet -> navigator.navigate(CreateWallet)
                Effect.NavigateToImportWallet -> navigator.navigate(ImportWallet)
                is Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    ) { state, onIntent ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val activeWallet = state.wallets.find { it.id == state.activeWalletId }
                        Text(
                            text = activeWallet?.name ?: "My Wallets",
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    actions = {
                        IconButton(onClick = { showWalletSelector = true }) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Wallets",
                                tint = MaterialTheme.colorScheme.onSurface,
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
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    state.wallets.isEmpty() -> {
                        EmptyWalletsContent(
                            onCreateClick = { onIntent(Intent.CreateNewWallet) },
                            onImportClick = { onIntent(Intent.ImportWallet) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        WalletListContent(
                            wallets = state.wallets,
                            activeWalletId = state.activeWalletId,
                            onSelectClick = { onIntent(Intent.SelectWallet(it)) },
                            onDeleteClick = { onIntent(Intent.DeleteWallet(it)) },
                            onCreateClick = { onIntent(Intent.CreateNewWallet) },
                            onImportClick = { onIntent(Intent.ImportWallet) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWalletsContent(
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No wallets yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create a new wallet or import an existing one",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        WalletActionButtons(
            onCreateClick = onCreateClick,
            onImportClick = onImportClick
        )
    }
}

@Composable
private fun WalletListContent(
    wallets: List<Wallet>,
    activeWalletId: String?,
    onSelectClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(
                items = wallets,
                key = { _, wallet -> wallet.id }
            ) { index, wallet ->
                WalletCard(
                    wallet = wallet,
                    isActive = wallet.id == activeWalletId,
                    position = cardPosition(index, wallets.size),
                    onSelectClick = { onSelectClick(wallet.id) },
                    onDeleteClick = { onDeleteClick(wallet.id) },
                )
            }
        }

        WalletActionButtons(
            onCreateClick = onCreateClick,
            onImportClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun WalletCard(
    wallet: Wallet,
    isActive: Boolean,
    position: CardPosition,
    onSelectClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    GroupCard(
        position = position,
        onClick = onSelectClick,
        title = wallet.name,
        subtitle = shortenAddress(wallet.address),
        icon = CardIcon.Letter(wallet.name.firstOrNull()?.uppercaseChar() ?: 'W'),
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active wallet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete wallet",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
private fun WalletActionButtons(
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCreateClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(91.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Create new",
                style = MaterialTheme.typography.labelLarge
            )
        }

        OutlinedButton(
            onClick = onImportClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(91.dp)
        ) {
            Text(
                text = "Import",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun shortenAddress(address: String): String {
    return if (address.length > 10) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}
