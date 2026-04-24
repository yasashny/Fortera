package com.yasashny.fortera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yasashny.fortera.core.common.currency.Currency
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.navigation.AppNavigator
import com.yasashny.fortera.core.navigation.ForteraNavHost
import com.yasashny.fortera.core.navigation.NavigationRegistry
import com.yasashny.fortera.core.network.currency.CurrencyRepository
import com.yasashny.fortera.core.network.currency.FiatRateRepository
import com.yasashny.fortera.core.network.currency.FiatRates
import com.yasashny.fortera.core.ui.LocalSnackbarHostState
import com.yasashny.fortera.core.ui.currency.FiatDisplay
import com.yasashny.fortera.core.ui.currency.LocalFiat
import com.yasashny.fortera.lock.AppLockGate
import org.koin.compose.koinInject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForteraTheme {
                val fiatRateRepository = koinInject<FiatRateRepository>()

                // Kick off before the lock screen so rates are ready by the time the user unlocks.
                // The call short-circuits if last fetch is within the staleness window.
                LaunchedEffect(Unit) { fiatRateRepository.refreshIfStale() }

                AppLockGate {
                    val navigator = koinInject<AppNavigator>()
                    val registry = koinInject<NavigationRegistry>()
                    val currencyRepository = koinInject<CurrencyRepository>()
                    val snackbarHostState = remember { SnackbarHostState() }

                    val currency by currencyRepository.observe()
                        .collectAsState(initial = Currency.Default)
                    val rates by fiatRateRepository.observeRates()
                        .collectAsState(initial = FiatRates.Empty)
                    val fiat = remember(currency, rates) {
                        FiatDisplay(currency, rates.rateFor(currency))
                    }

                    CompositionLocalProvider(
                        LocalSnackbarHostState provides snackbarHostState,
                        LocalFiat provides fiat,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ForteraNavHost(navigator = navigator, registry = registry)
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.align(Alignment.BottomCenter),
                            )
                        }
                    }
                }
            }
        }
    }
}
