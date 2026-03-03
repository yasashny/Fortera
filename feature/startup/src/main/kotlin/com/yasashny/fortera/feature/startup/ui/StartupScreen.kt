package com.yasashny.fortera.feature.startup.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.mvi.MviContainer
import com.yasashny.fortera.core.navigation.LocalAppNavigator
import com.yasashny.fortera.core.ui.component.CardGroup
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.feature.createwallet.CreateWallet
import com.yasashny.fortera.feature.importwallet.ImportWallet
import com.yasashny.fortera.feature.main.Main
import com.yasashny.fortera.feature.startup.presentation.StartupContract
import com.yasashny.fortera.feature.startup.presentation.StartupContract.Effect
import com.yasashny.fortera.feature.startup.presentation.StartupViewModel
import org.koin.androidx.compose.koinViewModel
import com.yasashny.fortera.core.ui.R as CoreR
import com.yasashny.fortera.feature.startup.R as StartupR

@Composable
internal fun StartupScreen(
    modifier: Modifier = Modifier,
    viewModel: StartupViewModel = koinViewModel()
) {
    val navigator = LocalAppNavigator.current
    MviContainer(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                Effect.NavigateToCreateWallet -> navigator.navigate(CreateWallet)
                Effect.NavigateToImportWallet -> navigator.navigate(ImportWallet)
                Effect.NavigateToMain -> navigator.clearAndNavigate(Main)
            }
        }
    ) { state, onIntent ->
        if (state.isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            StartupLayout(
                modifier = modifier,
                onIntent
            )
        }
    }
}

@Composable
private fun StartupLayout(
    modifier: Modifier = Modifier,
    onIntent: (StartupContract.Intent) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), MaterialTheme.colorScheme.background),
                    startY = 0f,
                    endY = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.weight(1f))

            AnimatedLogo(painterResource(id = StartupR.drawable.logo))

            Spacer(modifier = Modifier.weight(1f))

            CardGroup {
                GroupCard(
                    position = CardPosition.First,
                    onClick = { onIntent(StartupContract.Intent.CreateWalletClicked) },
                    title = stringResource(StartupR.string.startup_create_new_wallet),
                    icon = CardIcon.Resource(CoreR.drawable.ic_add)
                )

                GroupCard(
                    position = CardPosition.Last,
                    onClick = { onIntent(StartupContract.Intent.ImportWalletClicked) },
                    title = stringResource(StartupR.string.startup_import_wallet),
                    icon = CardIcon.Resource(CoreR.drawable.ic_download)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StartupLayoutPreview() {
    ForteraTheme {
        StartupLayout(onIntent = {})
    }
}
