package com.yasashny.fortera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.navigation.AppNavigator
import com.yasashny.fortera.core.navigation.ForteraNavHost
import com.yasashny.fortera.core.navigation.NavigationRegistry
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForteraTheme {
                val navigator = koinInject<AppNavigator>()
                val registry = koinInject<NavigationRegistry>()
                val snackbarHostState = remember { SnackbarHostState() }
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalSnackbarHostState provides snackbarHostState
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ForteraNavHost(navigator = navigator, registry = registry)
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}