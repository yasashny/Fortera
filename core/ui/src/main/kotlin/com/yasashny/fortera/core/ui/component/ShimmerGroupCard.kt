package com.yasashny.fortera.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme

@Composable
fun ShimmerGroupCard(
    position: CardPosition,
    modifier: Modifier = Modifier,
    showTrailing: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = cardShapeForPosition(position),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 16.dp))
                ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 14.dp))
            }
            if (showTrailing) {
                Spacer(modifier = Modifier.width(16.dp))
                ShimmerBox(modifier = Modifier.size(width = 70.dp, height = 16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShimmerGroupCardPreview() {
    ForteraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ShimmerGroupCard(position = CardPosition.First)
            ShimmerGroupCard(position = CardPosition.Middle)
            ShimmerGroupCard(position = CardPosition.Last)
        }
    }
}
