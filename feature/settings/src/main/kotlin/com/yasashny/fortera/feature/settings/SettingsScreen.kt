package com.yasashny.fortera.feature.settings

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.feature.settings.SettingsContract.Effect
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SettingsScreen() {
    val navigator = LocalAppNavigator.current
    val context = LocalContext.current
    val viewModel: SettingsViewModel = koinViewModel()

    var pendingEnable by remember { mutableStateOf(false) }
    val enableTitle = stringResource(R.string.settings_biometric_enable_title)
    val disableTitle = stringResource(R.string.settings_biometric_disable_title)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onBiometricConfirmed(pendingEnable)
        }
    }

    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is Effect.RequestBiometric -> {
                    pendingEnable = effect.enable
                    val km = context.getSystemService(KeyguardManager::class.java)
                    val title = if (effect.enable) enableTitle else disableTitle
                    val intent = km?.createConfirmDeviceCredentialIntent(title, null)
                    if (intent != null) {
                        launcher.launch(intent)
                    } else {
                        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    }
                }
            }
        },
    ) { state, onIntent ->
        SettingsLayout(
            state = state,
            onIntent = onIntent,
            onBack = { navigator.back() },
        )
    }
}
