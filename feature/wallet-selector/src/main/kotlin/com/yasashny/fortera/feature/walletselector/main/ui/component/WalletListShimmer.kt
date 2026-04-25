package com.yasashny.fortera.feature.walletselector.main.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.ShimmerGroupCard
import com.yasashny.fortera.core.ui.component.cardPosition

private const val ShimmerItemCount = 3

@Composable
internal fun WalletListShimmer(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(ShimmerItemCount) { index ->
            ShimmerGroupCard(
                position = cardPosition(index, ShimmerItemCount),
                showTrailing = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WalletListShimmerPreview() {
    ForteraTheme {
        WalletListShimmer(modifier = Modifier.padding(16.dp))
    }
}
