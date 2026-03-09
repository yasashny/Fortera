package com.yasashny.fortera.feature.main.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import kotlinx.coroutines.delay

@Composable
internal fun ErrorBanner(
    modifier: Modifier = Modifier,
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                onClick = onDismiss,
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    if (visible) {
        LaunchedEffect(Unit) {
            delay(3000)
            onDismiss()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorBannerPreview() {
    ForteraTheme {
        ErrorBanner(
            visible = true,
            message = "Сеть Ethereum недоступна",
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorBannerDarkPreview() {
    ForteraTheme {
        ErrorBanner(
            visible = true,
            message = "Сеть Ethereum недоступна",
            onDismiss = {},
        )
    }
}
