package com.yasashny.fortera.feature.main.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
internal fun rememberStaleAlpha(stale: Boolean): Float {
    if (!stale) return 1f
    val transition = rememberInfiniteTransition(label = "staleAlpha")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "staleAlpha",
    )
    return alpha
}
