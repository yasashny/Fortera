package com.yasashny.fortera.feature.receive.sendsuccess

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasashny.fortera.core.common.Haptics
import com.yasashny.fortera.core.designsystem.theme.ForteraTheme
import org.koin.compose.koinInject
import com.yasashny.fortera.feature.receive.R as ReceiveR
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SendSuccessLayout(
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
                SuccessAnimation()
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
private val ShapeSize = 140.dp
private val StageSize = 200.dp

@Composable
private fun SuccessAnimation() {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val inversePrimary = MaterialTheme.colorScheme.inversePrimary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface

    val haptics = koinInject<Haptics>()

    val timeline = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        haptics.click()
        timeline.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = TIMELINE_MS, easing = LinearEasing),
        )
    }

    val breathing = rememberInfiniteTransition(label = "breathing")
    val breatheScale by breathing.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    val particles = remember {
        List(10) { i ->
            val angle = (i.toFloat() / 10f) * (2f * PI.toFloat()) +
                if (i % 2 == 0) -0.18f else 0.18f
            val distanceDp = 88f + (i % 3) * 14f
            Particle(angle = angle, distanceDp = distanceDp, colorIndex = i % 3)
        }
    }

    val t = timeline.value
    val shapeProg = mapRange(t, 0.05f, 0.55f, EaseOutBack)
    val shapeBaseScale = 0.6f + 0.4f * shapeProg
    val shapeScale = shapeBaseScale * if (t >= 1f) breatheScale else 1f

    val morphT = mapRange(t, 0.40f, 1.00f, null)
    val morphFactor = (sin(morphT * PI.toFloat()) * 0.85f).coerceIn(0f, 1f)
    val cornerDp = (ShapeSize.value * 0.5f - (ShapeSize.value * 0.5f - ShapeSize.value * 0.38f) * morphFactor).dp

    val checkProg = mapRange(t, 0.30f, 0.75f, EaseOutCubic)

    val ring1Prog = mapRange(t, 0.35f, 1.00f, EaseOutCubic)
    val ring1Scale = 0.4f + 1.4f * ring1Prog
    val ring1Alpha = 0.35f * (1f - ring1Prog)

    val ring2Prog = mapRange(t, 0.55f, 1.00f, EaseOutCubic)
    val ring2Scale = 0.4f + 1.4f * ring2Prog
    val ring2Alpha = 0.30f * (1f - ring2Prog)

    val partProg = mapRange(t, 0.45f, 1.00f, EaseOutCubic)
    val partFade = mapRange(t, 0.45f, 1.00f, null)
    val partAlpha = 1f - partFade
    val partScale = 0.4f + 0.6f * partProg

    val titleProg = mapRange(t, 0.60f, 0.95f, EaseOutCubic)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(StageSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val shapeRadiusPx = ShapeSize.toPx() / 2f
                val strokePx = 2.dp.toPx()

                if (ring1Alpha > 0.001f) {
                    drawCircle(
                        color = primary.copy(alpha = ring1Alpha),
                        radius = shapeRadiusPx * ring1Scale,
                        center = center,
                        style = Stroke(width = strokePx),
                    )
                }
                if (ring2Alpha > 0.001f) {
                    drawCircle(
                        color = primary.copy(alpha = ring2Alpha),
                        radius = shapeRadiusPx * ring2Scale,
                        center = center,
                        style = Stroke(width = strokePx),
                    )
                }

                if (partAlpha > 0.001f) {
                    val particleSizePx = 10.dp.toPx() * partScale
                    val cornerPx = 3.dp.toPx()
                    particles.forEach { p ->
                        val dist = p.distanceDp.dp.toPx() * partProg
                        val px = center.x + cos(p.angle) * dist
                        val py = center.y + sin(p.angle) * dist
                        val col = when (p.colorIndex) {
                            0 -> primary
                            1 -> tertiary
                            else -> inversePrimary
                        }.copy(alpha = partAlpha)
                        drawRoundRect(
                            color = col,
                            topLeft = Offset(px - particleSizePx / 2f, py - particleSizePx / 2f),
                            size = Size(particleSizePx, particleSizePx),
                            cornerRadius = CornerRadius(cornerPx, cornerPx),
                        )
                    }
                }
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

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(ReceiveR.string.send_success),
            style = MaterialTheme.typography.headlineSmall,
            color = onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.graphicsLayer {
                translationY = (1f - titleProg) * 16.dp.toPx()
                alpha = titleProg
            },
        )
    }
}

@Composable
private fun AnimatedCheck(progress: Float, color: Color) {
    Canvas(modifier = Modifier.size(80.dp)) {
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
                width = 7.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private data class Particle(
    val angle: Float,
    val distanceDp: Float,
    val colorIndex: Int,
)

private fun mapRange(t: Float, start: Float, end: Float, easing: Easing?): Float {
    val raw = ((t - start) / (end - start)).coerceIn(0f, 1f)
    return easing?.transform(raw) ?: raw
}

@Preview(showBackground = true)
@Composable
private fun SendSuccessLayoutPreview() {
    ForteraTheme {
        SendSuccessLayout(
            onCloseClick = {},
        )
    }
}
