package com.yasashny.fortera.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val InputCardSpacing = 4.dp

@Composable
fun InputCard(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Single,
    enabled: Boolean = true,
    error: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShapeForPosition(position),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 16.dp),
            )
        }
    }
}

@Composable
fun InputCardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(InputCardSpacing),
        content = content,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun InputCardSinglePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            InputCard(
                value = "",
                onValueChange = {},
                placeholder = "Wallet name",
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun InputCardGroupPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            InputCardGroup {
                InputCard(
                    value = "My Wallet",
                    onValueChange = {},
                    placeholder = "Name",
                    position = CardPosition.First,
                )
                InputCard(
                    value = "",
                    onValueChange = {},
                    placeholder = "Secret phrase",
                    position = CardPosition.Last,
                    error = "Invalid seed phrase",
                )
            }
        }
    }
}
