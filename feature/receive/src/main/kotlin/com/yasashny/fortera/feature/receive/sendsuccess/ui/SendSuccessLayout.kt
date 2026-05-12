package com.yasashny.fortera.feature.receive.sendsuccess.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasashny.fortera.core.common.Haptics
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import org.koin.compose.koinInject
import com.yasashny.fortera.feature.receive.R as ReceiveR
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SendSuccessLayout(
    amount: String,
    symbol: String,
    onCloseClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(ReceiveR.string.send_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                SuccessAnimation(amount = amount, symbol = symbol)
            }

            Button(
                onClick = onCloseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(ReceiveR.string.send_close),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private const val TIMELINE_MS = 1400
private const val RING_DURATION_MS = 2400
private val ShapeSize = 116.dp
private val StageSize = 240.dp
private val HaloRingEasing = CubicBezierEasing(0.2f, 0.6f, 0.2f, 1f)

@Composable
private fun SuccessAnimation(amount: String, symbol: String) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val haptics = koinInject<Haptics>()

    val timeline = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        haptics.click()
        timeline.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = TIMELINE_MS, easing = LinearEasing),
        )
    }

    val infinite = rememberInfiniteTransition(label = "success")
    val breatheScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val ring1Phase by infinite.haloRingPhase(delayMs = 0, label = "ring1")
    val ring2Phase by infinite.haloRingPhase(delayMs = 800, label = "ring2")
    val ring3Phase by infinite.haloRingPhase(delayMs = 1600, label = "ring3")

    val t = timeline.value
    val revealProg = mapRange(t, 0.05f, 0.55f, EaseOutBack)
    val shapeBaseScale = 0.6f + 0.4f * revealProg
    val shapeScale = shapeBaseScale * if (t >= 1f) breatheScale else 1f

    val morphT = mapRange(t, 0.40f, 1.00f, null)
    val morphFactor = (sin(morphT * PI.toFloat()) * 0.85f).coerceIn(0f, 1f)
    val cornerDp = (ShapeSize.value * 0.5f - (ShapeSize.value * 0.5f - ShapeSize.value * 0.38f) * morphFactor).dp

    val checkProg = mapRange(t, 0.30f, 0.75f, EaseOutCubic)
    val amountProg = mapRange(t, 0.60f, 0.95f, EaseOutCubic)
    val haloReveal = mapRange(t, 0.00f, 0.35f, EaseOutCubic)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(StageSize),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = haloReveal }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.18f),
                                primary.copy(alpha = 0f),
                            ),
                        ),
                    ),
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadiusPx = ShapeSize.toPx() / 2f
                val strokePx = 1.dp.toPx()

                drawHaloRing(center, baseRadiusPx, strokePx, ring1Phase, primary, haloReveal)
                drawHaloRing(center, baseRadiusPx, strokePx, ring2Phase, primary, haloReveal)
                drawHaloRing(center, baseRadiusPx, strokePx, ring3Phase, primary, haloReveal)
            }

            Box(
                modifier = Modifier
                    .size(ShapeSize)
                    .scale(shapeScale)
                    .clip(RoundedCornerShape(cornerDp))
                    .background(primary),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedCheck(progress = checkProg, color = onPrimary)
            }
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = "$amount $symbol",
            color = primary,
            fontSize = 38.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.8).sp,
            modifier = Modifier.graphicsLayer {
                translationY = (1f - amountProg) * 16.dp.toPx()
                alpha = amountProg
            },
        )
    }
}

@Composable
private fun InfiniteTransition.haloRingPhase(
    delayMs: Int,
    label: String,
) = animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = RING_DURATION_MS, easing = HaloRingEasing),
        repeatMode = RepeatMode.Restart,
        initialStartOffset = StartOffset(offsetMillis = delayMs),
    ),
    label = label,
)

private fun DrawScope.drawHaloRing(
    center: Offset,
    baseRadiusPx: Float,
    strokePx: Float,
    phase: Float,
    color: Color,
    reveal: Float,
) {
    if (reveal <= 0.001f) return
    val scale = 1.05f + 0.55f * phase
    val alpha = when {
        phase < 0.2f -> (phase / 0.2f) * 0.35f
        else -> 0.35f * (1f - (phase - 0.2f) / 0.8f)
    } * reveal
    if (alpha <= 0.001f) return
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = baseRadiusPx * scale,
        center = center,
        style = Stroke(width = strokePx),
    )
}

@Composable
private fun AnimatedCheck(progress: Float, color: Color) {
    Canvas(modifier = Modifier.size(58.dp)) {
        val w = size.width
        val h = size.height
        val start = Offset(0.22f * w, 0.52f * h)
        val mid = Offset(0.44f * w, 0.72f * h)
        val end = Offset(0.78f * w, 0.30f * h)

        val len1 = hypot(mid.x - start.x, mid.y - start.y)
        val len2 = hypot(end.x - mid.x, end.y - mid.y)
        val total = len1 + len2
        val drawn = progress * total

        val path = Path().apply {
            moveTo(start.x, start.y)
            if (drawn <= len1) {
                val f = if (len1 > 0f) drawn / len1 else 0f
                lineTo(start.x + (mid.x - start.x) * f, start.y + (mid.y - start.y) * f)
            } else {
                lineTo(mid.x, mid.y)
                val f = if (len2 > 0f) (drawn - len1) / len2 else 0f
                lineTo(mid.x + (end.x - mid.x) * f, mid.y + (end.y - mid.y) * f)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private fun mapRange(t: Float, start: Float, end: Float, easing: Easing?): Float {
    val raw = ((t - start) / (end - start)).coerceIn(0f, 1f)
    return easing?.transform(raw) ?: raw
}

@Preview(showBackground = true)
@Composable
private fun SendSuccessLayoutPreview() {
    ForteraTheme {
        SendSuccessLayout(
            amount = "0.000345",
            symbol = "ETH",
            onCloseClick = {},
        )
    }
}
