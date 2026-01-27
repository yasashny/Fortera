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
import com.yasashny.fortera.core.ui.R

/** Spacing between cards in a group */
private val CardSpacing = 4.dp

/** Width of text fade zone */
private val FadeWidth = 24.dp

/**
 * Modifier to create a horizontal fading edge effect on the right side.
 */
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

/**
 * Card position within a group.
 */
enum class CardPosition {
    /** Single card - all corners rounded */
    Single,
    /** First card in group - top corners rounded */
    First,
    /** Middle card in group - no rounded corners */
    Middle,
    /** Last card in group - bottom corners rounded */
    Last
}

/**
 * Icon for GroupCard - can be a drawable resource, ImageVector, or letter.
 */
sealed interface CardIcon {
    /** Icon from drawable resource */
    data class Resource(@DrawableRes val resId: Int) : CardIcon

    /** Icon from ImageVector */
    data class Vector(val imageVector: ImageVector) : CardIcon

    /** Letter as placeholder */
    data class Letter(val char: Char) : CardIcon
}

/**
 * Returns card shape based on its position in a group.
 */
@Composable
fun cardShapeForPosition(
    position: CardPosition,
    cornerRadius: Dp = 16.dp
): Shape {
    return when (position) {
        CardPosition.Single -> RoundedCornerShape(cornerRadius)
        CardPosition.First -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
        CardPosition.Middle -> RoundedCornerShape(0.dp)
        CardPosition.Last -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )
    }
}

/**
 * Card with icon, title/subtitle and trailing content.
 *
 * @param position Card position in group
 * @param onClick Click handler
 * @param title Main text
 * @param modifier Modifier
 * @param subtitle Subtitle (displayed below title)
 * @param iconUrl Image URL (loaded via Coil)
 * @param icon Icon (Resource, Vector or Letter)
 * @param cornerRadius Corner radius
 * @param trailing Trailing content (e.g., chevron, switch, value)
 */
@Composable
fun GroupCard(
    position: CardPosition,
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconUrl: String? = null,
    icon: CardIcon? = null,
    cornerRadius: Dp = 16.dp,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShapeForPosition(position, cornerRadius),
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
            CardIconContent(
                iconUrl = iconUrl,
                icon = icon
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalFadingEdge(),
                verticalArrangement = if (subtitle != null) Arrangement.spacedBy(2.dp) else Arrangement.Center
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
                    softWrap = false
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * Card icon content with Coil support and fallback.
 */
@Composable
private fun CardIconContent(
    iconUrl: String?,
    icon: CardIcon?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Load image from URL
            iconUrl != null -> {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            icon != null -> {
                IconWithBackground(icon = icon, size = size)
            }
            else -> {
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
    }
}

/**
 * Icon with circular background.
 */
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

/**
 * Avatar with letter.
 */
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

// ============== Card group containers ==============

/**
 * Container for a group of cards (Column).
 * Automatically adds spacing between cards.
 *
 * @param modifier Modifier
 * @param content Content - cards
 */
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

/**
 * Convenient container for a list of similar cards.
 * Automatically determines position of each card.
 *
 * @param items List of items
 * @param modifier Modifier
 * @param key Function to get unique key for item
 * @param itemContent Content for each item (receives item and position)
 */
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

/**
 * Container for a group of cards with LazyColumn.
 * Automatically determines position of each card and adds spacing.
 *
 * @param items List of items
 * @param modifier Modifier
 * @param contentPadding Content padding for the list
 * @param key Function to get unique key for item
 * @param itemContent Content for each item (receives item and position)
 */
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

/**
 * Extension for LazyListScope - adds a group of cards to LazyColumn.
 *
 * @param items List of items
 * @param key Function to get unique key for item
 * @param itemContent Content for each item (receives item and position)
 */
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

/**
 * Determines card position by index and total count.
 */
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