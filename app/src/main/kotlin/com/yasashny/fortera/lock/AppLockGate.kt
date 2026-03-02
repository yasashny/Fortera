package com.yasashny.fortera.lock

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.yasashny.fortera.R
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.compose.koinViewModel
import kotlin.coroutines.resume

@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    val viewModel: AppLockViewModel = koinViewModel()
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    when (state) {
        AuthState.Unlocked -> content()
        AuthState.Loading, AuthState.Locked -> {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }
    }

    if (state == AuthState.Locked) {
        BiometricAuthEffect(
            onAuthenticated = viewModel::onAuthenticated,
            onCancelled = { (context as Activity).finish() },
        )
    }
}

@Composable
private fun BiometricAuthEffect(
    onAuthenticated: () -> Unit,
    onCancelled: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val title = stringResource(R.string.app_lock_title)
    val subtitle = stringResource(R.string.app_lock_subtitle)

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val authenticated = suspendCancellableCoroutine { cont ->
                val activity = context as FragmentActivity
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        if (cont.isActive) cont.resume(true)
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        // ERROR_CANCELED = system dismiss (e.g. app backgrounded)
                        // repeatOnLifecycle will cancel the coroutine and retry on next RESUME
                        if (errorCode != BiometricPrompt.ERROR_CANCELED && cont.isActive) {
                            cont.resume(false)
                        }
                    }
                }

                val prompt = BiometricPrompt(activity, callback)
                cont.invokeOnCancellation { prompt.cancelAuthentication() }

                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                    )
                    .build()

                prompt.authenticate(info)
            }

            if (authenticated) onAuthenticated() else onCancelled()
        }
    }
}
