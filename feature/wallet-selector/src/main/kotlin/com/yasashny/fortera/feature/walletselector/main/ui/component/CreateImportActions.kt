package com.yasashny.fortera.feature.walletselector.main.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import com.yasashny.fortera.core.ui.R as CoreR
import com.yasashny.fortera.core.ui.component.CardGroup
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.feature.walletselector.R

/**
 * Top of the wallet-selector sheet: the two always-visible entry points to the create-wallet
 * and import-wallet flows.
 */
@Composable
internal fun CreateImportActions(
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardGroup(modifier = modifier) {
        GroupCard(
            position = CardPosition.First,
            onClick = onCreateClick,
            title = stringResource(R.string.wallet_selector_create_new_wallet),
            icon = CardIcon.Resource(CoreR.drawable.ic_add),
        )
        GroupCard(
            position = CardPosition.Last,
            onClick = onImportClick,
            title = stringResource(R.string.wallet_selector_import_wallet),
            icon = CardIcon.Resource(CoreR.drawable.ic_download),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateImportActionsPreview() {
    ForteraTheme {
        CreateImportActions(
            onCreateClick = {},
            onImportClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
