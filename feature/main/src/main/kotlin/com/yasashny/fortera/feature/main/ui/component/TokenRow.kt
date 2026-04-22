package com.yasashny.fortera.feature.main.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.domaincrypto.model.TokenBalance
import com.yasashny.fortera.core.ui.component.CardIcon
import com.yasashny.fortera.core.ui.component.CardPosition
import com.yasashny.fortera.core.ui.component.GroupCard
import com.yasashny.fortera.core.ui.format.formatCrypto
import com.yasashny.fortera.core.ui.token.tokenIconUrl
import com.yasashny.fortera.core.ui.token.networkBadgeUrlFor
import java.util.Locale

private val ChangePositive = Color(0xFF02A64C)
private val ChangeNegative = Color(0xFFD0081C)

@Composable
internal fun TokenRow(
    tokenBalance: TokenBalance,
    position: CardPosition,
    staleAlpha: Float,
    onClick: () -> Unit,
) {
    val change = tokenBalance.changePercent24h
    val changeColor = when {
        change > 0 -> ChangePositive
        change < 0 -> ChangeNegative
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    GroupCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        position = position,
        onClick = onClick,
        title = tokenBalance.token.name,
        subtitle = String.format(Locale.US, "%+.2f%%", change),
        subtitleColor = changeColor.copy(alpha = staleAlpha),
        iconUrl = tokenIconUrl(tokenBalance.token.symbol),
        icon = CardIcon.Letter(tokenBalance.token.symbol.first()),
        badgeIconUrl = networkBadgeUrlFor(tokenBalance.token),
        trailing = {
            Text(
                text = "${formatCrypto(tokenBalance.balance)} ${tokenBalance.token.symbol}",
                modifier = Modifier.alpha(staleAlpha),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        },
    )
}
