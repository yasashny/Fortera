package com.yasashny.fortera.feature.settings

import android.content.Intent as AndroidIntent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.network.environment.AppEnvironment
import com.yasashny.fortera.core.ui.component.CardGroup
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.feature.settings.SettingsContract.Intent
import com.yasashny.fortera.feature.settings.SettingsContract.State
import java.util.Locale
import com.yasashny.fortera.feature.settings.R as SettingsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsLayout(
    state: State,
    onIntent: (Intent) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    val currentLanguage = remember {
        if (Locale.getDefault().language == "ru") "Русский" else "English"
    }

    val openLanguageSettings: () -> Unit = remember {
        {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AndroidIntent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            } else {
                AndroidIntent(Settings.ACTION_LOCALE_SETTINGS)
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(SettingsR.string.settings_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(SettingsR.string.settings_back_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            CardGroup {
                GroupCard(
                    position = CardPosition.First,
                    onClick = openLanguageSettings,
                    title = stringResource(SettingsR.string.settings_language),
                    subtitle = currentLanguage,
                    icon = CardIcon.Vector(Icons.Default.Language),
                )
                GroupCard(
                    position = CardPosition.Middle,
                    onClick = {},
                    title = stringResource(SettingsR.string.settings_currency),
                    subtitle = "USD",
                    icon = CardIcon.Vector(Icons.Default.AttachMoney),
                )
                GroupCard(
                    position = CardPosition.Last,
                    onClick = { onIntent(Intent.TogglePassword) },
                    title = stringResource(SettingsR.string.settings_password),
                    icon = CardIcon.Vector(Icons.Default.Lock),
                    trailing = {
                        Switch(
                            checked = state.isPasswordEnabled,
                            onCheckedChange = { onIntent(Intent.TogglePassword) },
                        )
                    },
                )
            }

            if (state.isDebugMode) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(SettingsR.string.settings_debug_section),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                )
                CardGroup {
                    GroupCard(
                        position = CardPosition.Single,
                        onClick = { onIntent(Intent.OpenEnvironmentSheet) },
                        title = stringResource(SettingsR.string.settings_environment),
                        subtitle = state.environment.displayName,
                        icon = CardIcon.Vector(Icons.Default.Tune),
                    )
                }
            }
        }
    }

    if (state.isEnvSheetOpen) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { onIntent(Intent.DismissEnvironmentSheet) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            EnvironmentSheetContent(
                current = state.environment,
                onSelect = { onIntent(Intent.SelectEnvironment(it)) },
            )
        }
    }
}

@Composable
private fun EnvironmentSheetContent(
    current: AppEnvironment,
    onSelect: (AppEnvironment) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(SettingsR.string.settings_environment_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(SettingsR.string.settings_environment_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        AppEnvironment.entries.forEach { env ->
            val selected = env == current
            GroupCard(
                position = CardPosition.Single,
                onClick = { onSelect(env) },
                title = env.displayName,
                subtitle = if (env.isTestnet) "Sepolia / BTC testnet" else "Mainnet",
                icon = CardIcon.Vector(Icons.Default.Tune),
                trailing = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else null,
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsLayoutPreview() {
    ForteraTheme {
        SettingsLayout(
            state = State(
                isPasswordEnabled = false,
                isLoading = false,
                isDebugMode = true,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
