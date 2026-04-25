package com.yasashny.fortera.feature.walletselector.settings.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.component.ShimmerBox

/**
 * Placeholder shown while the wallet record is being read from the database.
 * Sized to match the real [WalletNameField] so the layout doesn't shift on load.
 */
@Composable
internal fun WalletSettingsShimmer(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .size(width = 0.dp, height = 64.dp),
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WalletSettingsShimmerPreview() {
    ForteraTheme {
        WalletSettingsShimmer(modifier = Modifier.padding(16.dp))
    }
}
