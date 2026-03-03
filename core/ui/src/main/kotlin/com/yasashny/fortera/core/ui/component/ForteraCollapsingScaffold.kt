package com.yasashny.fortera.core.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DefaultCollapsedBarHeight = 64.dp

@Composable
fun ForteraCollapsingScaffold(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    collapsedBarHeight: Dp = DefaultCollapsedBarHeight,
    @DrawableRes backgroundImage: Int? = null,
    listState: LazyListState = rememberLazyListState(),
    bottomContentPadding: Dp = 0.dp,
    collapsedBar: @Composable (fraction: Float) -> Unit,
    expandedHeader: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val collapsedBarTotalPx = with(density) { (collapsedBarHeight + statusBarHeight).roundToPx() }

    // 0f = header at rest, 1f = header scrolled off behind collapsed bar
    // Uses only Int pixel values (beforeContentPadding, offset, size) to avoid Float/Int mismatch
    val fraction by remember {
        derivedStateOf {
            // Guaranteed 0 when list is at rest — no rounding issues possible
            if (listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0
            ) return@derivedStateOf 0f

            val info = listState.layoutInfo
            if (info.viewportSize.height == 0 || info.visibleItemsInfo.isEmpty()) {
                return@derivedStateOf 0f
            }

            val headerInfo = info.visibleItemsInfo
                .firstOrNull { it.key == "collapsing_header" }
                ?: return@derivedStateOf 1f

            val headerBottom = headerInfo.offset + headerInfo.size
            val restBottom = info.beforeContentPadding + headerInfo.size
            val range = restBottom - collapsedBarTotalPx
            if (range <= 0) 0f
            else (1f - (headerBottom - collapsedBarTotalPx).toFloat() / range.toFloat())
                .coerceIn(0f, 1f)
        }
    }

    // Exact deficit to allow full collapse even with little content
    val scrollReserveDp by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewport = info.viewportSize.height
            val headerItem = info.visibleItemsInfo
                .firstOrNull { it.key == "collapsing_header" }
            val maxScrollNeeded = headerItem
                ?.let { it.size + info.beforeContentPadding - collapsedBarTotalPx } ?: 0

            if (viewport <= 0 || maxScrollNeeded <= 0) {
                0.dp
            } else {
                val contentHeight = info.visibleItemsInfo
                    .filter { it.key != "collapsing_scroll_reserve" }
                    .sumOf { it.size }
                val totalWithPadding =
                    contentHeight + info.beforeContentPadding + info.afterContentPadding
                val deficit = viewport + maxScrollNeeded - totalWithPadding
                with(density) { deficit.coerceAtLeast(0).toDp() }
            }
        }
    }

    // Expanded header fades out in first half, collapsed bar fades in in second half
    val expandedAlpha = (1f - fraction * 2f).coerceAtLeast(0f)
    val collapsedAlpha = ((fraction - 0.4f) / 0.6f).coerceIn(0f, 1f)
    val bgAlpha = (fraction * 2f).coerceAtMost(1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        backgroundImage?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                alpha = expandedAlpha
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarHeight,
                bottom = bottomContentPadding,
            ),
        ) {
            item(key = "collapsing_header") {
                Box(modifier = Modifier.alpha(expandedAlpha)) {
                    expandedHeader()
                }
            }

            content()

            if (scrollReserveDp > 0.dp) {
                item(key = "collapsing_scroll_reserve") {
                    Spacer(modifier = Modifier.height(scrollReserveDp))
                }
            }
        }

        // Fixed collapsed bar overlay — not rendered until needed to avoid intercepting touches
        if (collapsedAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(collapsedAlpha)
                    .background(backgroundColor.copy(alpha = bgAlpha)),
            ) {
                collapsedBar(fraction)
            }
        }
    }
}
