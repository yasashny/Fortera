package com.yasashny.fortera.feature.main.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.feature.main.R
import com.yasashny.fortera.feature.main.ui.ActionButton

private val SendShape = RoundedCornerShape(
    topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 4.dp,
)
private val ReceiveShape = RoundedCornerShape(
    topStart = 4.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp,
)

@Composable
internal fun QuickActions(
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        ActionButton(
            icon = Icons.AutoMirrored.Filled.Send,
            label = stringResource(R.string.main_send),
            shape = SendShape,
            onClick = onSendClick,
        )
        Spacer(Modifier.width(4.dp))
        ActionButton(
            icon = Icons.Default.QrCodeScanner,
            label = stringResource(R.string.main_receive),
            shape = ReceiveShape,
            onClick = onReceiveClick,
        )
    }
}
