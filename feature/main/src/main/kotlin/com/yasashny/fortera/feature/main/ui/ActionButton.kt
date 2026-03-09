package com.yasashny.fortera.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme

@Composable
internal fun ActionButton(
    modifier: Modifier = Modifier,
    shape: Shape,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.size(100.dp, 78.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonPreview() {
    ForteraTheme {
        Row {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Send,
                label = "Send",
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 4.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 4.dp,
                ),
                onClick = {},
            )
            Spacer(Modifier.width(4.dp))
            ActionButton(
                icon = Icons.Default.QrCodeScanner,
                label = "Receive",
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 18.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 18.dp,
                ),
                onClick = {},
            )
        }
    }
}
