package com.yasashny.fortera.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.yasashny.fortera.core.ui.R

private val CardSpacing = 4.dp

private val FadeWidth = 24.dp

private fun Modifier.horizontalFadingEdge(
    fadeWidth: Dp = FadeWidth
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeWidthPx = fadeWidth.toPx()
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = size.width - fadeWidthPx,
                endX = size.width
            ), blendMode = BlendMode.DstIn
        )
    }

enum class CardPosition {
    Single,
    First,
    Middle,
    Last
}

sealed interface CardIcon {
    data class Resource(@DrawableRes val resId: Int) : CardIcon

    data class Vector(val imageVector: ImageVector) : CardIcon

    data class Letter(val char: Char) : CardIcon
}

@Composable
fun cardShapeForPosition(
    position: CardPosition,
    cornerRadius: Dp = 16.dp,
    innerRadius: Dp = 0.dp,
): Shape {
    return when (position) {
        CardPosition.Single -> RoundedCornerShape(cornerRadius)
        CardPosition.First -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        CardPosition.Middle -> RoundedCornerShape(innerRadius)
        CardPosition.Last -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )
    }
}

@Composable
fun GroupCard(
    position: CardPosition,
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    titleSuffix: String? = null,
    subtitle: String? = null,
    subtitleColor: Color? = null,
    iconUrl: String? = null,
    icon: CardIcon? = null,
    badgeIconUrl: String? = null,
    cornerRadius: Dp = 16.dp,
    innerRadius: Dp = 0.dp,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShapeForPosition(position, cornerRadius, innerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TokenIcon(
                iconUrl = iconUrl,
                icon = icon,
                badgeIconUrl = badgeIconUrl,
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalFadingEdge(),
                verticalArrangement = if (subtitle != null) Arrangement.spacedBy(2.dp) else Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = title,
                        style = if (subtitle != null) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        fontWeight = if (subtitle != null) FontWeight.Medium else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                    )
                    if (titleSuffix != null) {
                        Text(
                            text = titleSuffix,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailing()
            }
        }
    }
}

@Composable
fun TokenIcon(
    iconUrl: String?,
    icon: CardIcon?,
    modifier: Modifier = Modifier,
    badgeIconUrl: String? = null,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val fallback: @Composable () -> Unit = {
                if (icon != null) {
                    IconWithBackground(icon = icon, size = size)
                } else {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = CircleShape
                            )
                    )
                }
            }
            if (iconUrl.isNullOrBlank()) {
                fallback()
            } else {
                SubcomposeAsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = { fallback() },
                    loading = { fallback() },
                )
            }
        }
        if (badgeIconUrl != null) {
            val badgeSize = size * 0.4f
            AsyncImage(
                model = badgeIconUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = CircleShape,
                    )
                    .padding(1.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun IconWithBackground(
    icon: CardIcon,
    size: Dp = 40.dp
) {
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = backgroundColor, shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (icon) {
            is CardIcon.Resource -> {
                Icon(
                    painter = painterResource(icon.resId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }
            is CardIcon.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }
            is CardIcon.Letter -> {
                Text(
                    text = icon.char.uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun LetterAvatar(
    letter: Char,
    size: Dp = 40.dp
) {
    IconWithBackground(
        icon = CardIcon.Letter(letter),
        size = size
    )
}

@Composable
fun CardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
        content = content
    )
}

@Composable
fun <T> CardGroup(
    items: List<T>,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (item: T, position: CardPosition) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        items.forEachIndexed { index, item ->
            val position = cardPosition(index, items.size)
            if (key != null) {
                androidx.compose.runtime.key(key(item)) {
                    itemContent(item, position)
                }
            } else {
                itemContent(item, position)
            }
        }
    }
}

@Composable
fun <T> LazyCardGroup(
    items: List<T>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    key: ((T) -> Any)? = null,
    itemContent: @Composable (item: T, position: CardPosition) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        itemsIndexed(
            items = items,
            key = if (key != null) { _, item -> key(item) } else null
        ) { index, item ->
            val position = cardPosition(index, items.size)
            itemContent(item, position)
        }
    }
}

fun <T> LazyListScope.cardGroupItems(
    items: List<T>,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (item: T, position: CardPosition) -> Unit
) {
    itemsIndexed(
        items = items,
        key = if (key != null) { index, item -> key(item) } else null
    ) { index, item ->
        val position = cardPosition(index, items.size)

        if (index > 0) {
            Spacer(modifier = Modifier.size(CardSpacing))
        }

        itemContent(item, position)
    }
}

fun cardPosition(index: Int, total: Int): CardPosition {
    return when {
        total == 1 -> CardPosition.Single
        index == 0 -> CardPosition.First
        index == total - 1 -> CardPosition.Last
        else -> CardPosition.Middle
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun GroupCardWithIconPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GroupCard(
                position = CardPosition.Single,
                onClick = { },
                title = "Settings",
                icon = CardIcon.Letter('S')
            )
            GroupCard(
                position = CardPosition.Single,
                onClick = { },
                title = "Download",
                icon = CardIcon.Resource(R.drawable.ic_download)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun GroupCardWithTitleAndSubtitlePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GroupCard(
                position = CardPosition.Single,
                onClick = { },
                title = "Bitcoin",
                subtitle = "BTC • 0.00042",
                icon = CardIcon.Letter('B')
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun GroupCardWithLongTextFadePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GroupCard(
                position = CardPosition.Single,
                onClick = { },
                title = "Very Long Cryptocurrency Name That Should Fade",
                subtitle = "VLCN • This is a very long subtitle that should also fade out",
                icon = CardIcon.Letter('V'),
                trailing = {
                    Text(
                        text = "$999,999.99",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun GroupCardWithTrailingPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GroupCard(
                position = CardPosition.Single,
                onClick = { },
                title = "Ethereum",
                subtitle = "ETH",
                icon = CardIcon.Letter('E'),
                trailing = {
                    Text(
                        text = "$1,234.56",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun CardGroupAutoPositionPreview() {
    data class Crypto(val name: String, val symbol: String, val price: String)

    val cryptos = listOf(
        Crypto("Bitcoin", "BTC", "$45,000"),
        Crypto("Ethereum", "ETH", "$3,200"),
        Crypto("Solana", "SOL", "$98")
    )

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CardGroup(
                items = cryptos,
                key = { it.symbol }
            ) { crypto, position ->
                GroupCard(
                    position = position,
                    onClick = { },
                    title = crypto.name,
                    subtitle = crypto.symbol,
                    icon = CardIcon.Letter(crypto.symbol.first()),
                    trailing = {
                        Text(
                            text = crypto.price,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun CardGroupManualPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CardGroup {
                GroupCard(
                    position = CardPosition.First,
                    onClick = { },
                    title = "Create new wallet",
                    icon = CardIcon.Resource(R.drawable.ic_add)
                )
                GroupCard(
                    position = CardPosition.Last,
                    onClick = { },
                    title = "Import wallet",
                    icon = CardIcon.Resource(R.drawable.ic_download)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0D13)
@Composable
private fun LetterAvatarPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LetterAvatar(letter = 'A')
            LetterAvatar(letter = 'B')
            LetterAvatar(letter = 'C')
            LetterAvatar(letter = 'X')
        }
    }
}
