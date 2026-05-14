package com.yasashny.fortera.feature.receive.send.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as GeomSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.yasashny.fortera.feature.receive.R as ReceiveR
import java.util.concurrent.Executors

@Composable
internal fun QrScannerView(
    onClose: () -> Unit,
    onScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            if (!granted) onClose()
        },
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (hasPermission) {
            CameraPreview(onScanned = onScanned)
            ScannerOverlay()
            HintText(
                text = stringResource(ReceiveR.string.send_scan_hint),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else {
            HintText(
                text = stringResource(ReceiveR.string.send_scan_permission_needed),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        CloseButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp),
        )
    }
}

@Composable
private fun CameraPreview(onScanned: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnScanned by rememberUpdatedState(onScanned)
    val scanned = remember { mutableStateOf(false) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener(
                {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setResolutionSelector(analysisResolutionSelector())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            setAnalyzer(executor, QrCodeAnalyzer { value ->
                                if (!scanned.value) {
                                    scanned.value = true
                                    previewView.post { currentOnScanned(value) }
                                }
                            })
                        }

                    runCatching {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis,
                        )
                    }
                },
                ContextCompat.getMainExecutor(ctx),
            )

            previewView
        },
    )
}

@Composable
private fun ScannerOverlay() {
    val frameColor = MaterialTheme.colorScheme.primary
    val scrim = Color.Black.copy(alpha = 0.55f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val side = minOf(size.width, size.height) * 0.68f
        val left = (size.width - side) / 2f
        val top = (size.height - side) / 2f
        val right = left + side
        val bottom = top + side
        val cornerLen = side * 0.16f
        val strokePx = 4.dp.toPx()

        drawRect(scrim, Offset.Zero, GeomSize(size.width, top))
        drawRect(scrim, Offset(0f, bottom), GeomSize(size.width, size.height - bottom))
        drawRect(scrim, Offset(0f, top), GeomSize(left, side))
        drawRect(scrim, Offset(right, top), GeomSize(size.width - right, side))

        drawCorner(Offset(left, top), dx = 1, dy = 1, cornerLen, strokePx, frameColor)
        drawCorner(Offset(right, top), dx = -1, dy = 1, cornerLen, strokePx, frameColor)
        drawCorner(Offset(left, bottom), dx = 1, dy = -1, cornerLen, strokePx, frameColor)
        drawCorner(Offset(right, bottom), dx = -1, dy = -1, cornerLen, strokePx, frameColor)
    }
}

private fun DrawScope.drawCorner(
    origin: Offset,
    dx: Int,
    dy: Int,
    length: Float,
    strokeWidth: Float,
    color: Color,
) {
    drawLine(color, origin, Offset(origin.x + length * dx, origin.y), strokeWidth)
    drawLine(color, origin, Offset(origin.x, origin.y + length * dy), strokeWidth)
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50)),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(ReceiveR.string.send_close),
            tint = Color.White,
        )
    }
}

@Composable
private fun HintText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 32.dp, vertical = 64.dp),
    )
}

private fun analysisResolutionSelector(): ResolutionSelector =
    ResolutionSelector.Builder()
        .setResolutionStrategy(
            ResolutionStrategy(
                Size(1280, 720),
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
            ),
        )
        .build()

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private class QrCodeAnalyzer(
    private val onDecoded: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
            ),
        )
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        try {
            val text = runCatching { imageProxy.decodeQr() }.getOrNull()
            if (text != null) onDecoded(text)
        } finally {
            imageProxy.close()
        }
    }

    @ExperimentalGetImage
    private fun ImageProxy.decodeQr(): String? {
        val plane = image?.planes?.firstOrNull() ?: return null
        val data = ByteArray(plane.buffer.remaining()).also { plane.buffer.get(it) }
        val source = PlanarYUVLuminanceSource(
            data,
            plane.rowStride,
            height,
            0,
            0,
            width,
            height,
            false,
        )
        return try {
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
        } catch (_: NotFoundException) {
            null
        } finally {
            reader.reset()
        }
    }
}
