package ru.zapasli.app.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun BarcodeScannerDialog(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var cameraUnavailable by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { permissionGranted = it },
    )

    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("barcode_scanner"),
            color = Color.Black,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (permissionGranted && !cameraUnavailable) {
                    CameraPreview(
                        onBarcodeDetected = onBarcodeDetected,
                        onCameraUnavailable = { cameraUnavailable = true },
                    )
                    ScannerFrame(modifier = Modifier.align(Alignment.Center))
                }

                ScannerTopBar(
                    onDismiss = onDismiss,
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                when {
                    !permissionGranted -> ScannerProblemCard(
                        title = stringResource(R.string.scanner_permission_title),
                        description = stringResource(R.string.scanner_permission_description),
                        primaryAction = stringResource(R.string.grant_camera_access),
                        onPrimaryAction = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onManualEntry = onDismiss,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    cameraUnavailable -> ScannerProblemCard(
                        title = stringResource(R.string.scanner_unavailable_title),
                        description = stringResource(R.string.scanner_unavailable_description),
                        primaryAction = null,
                        onPrimaryAction = null,
                        onManualEntry = onDismiss,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> ScannerHint(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onManualEntry = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerTopBar(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.64f))
            .statusBarsPadding()
            .padding(horizontal = ZapasliSpacing.sm, vertical = ZapasliSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.scanner_title),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
        )
        TextButton(onClick = onDismiss) {
            Text(
                text = stringResource(R.string.close),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ScannerFrame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(1.55f)
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(28.dp),
            )
            .testTag("scanner_frame"),
    )
}

@Composable
private fun ScannerHint(
    onManualEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Black.copy(alpha = 0.76f),
    ) {
        Column(
            modifier = Modifier.padding(ZapasliSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.scanner_hint),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.scanner_privacy_note),
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(
                modifier = Modifier.testTag("scanner_manual_entry"),
                onClick = onManualEntry,
            ) {
                Text(
                    text = stringResource(R.string.enter_barcode_manually),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ScannerProblemCard(
    title: String,
    description: String,
    primaryAction: String?,
    onPrimaryAction: (() -> Unit)?,
    onManualEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(ZapasliSpacing.lg),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(ZapasliSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            primaryAction?.let { label ->
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanner_permission_request"),
                    onClick = { onPrimaryAction?.invoke() },
                ) {
                    Text(label)
                }
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanner_manual_entry"),
                onClick = onManualEntry,
            ) {
                Text(stringResource(R.string.enter_barcode_manually))
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    onCameraUnavailable: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val currentBarcodeCallback = androidx.compose.runtime.rememberUpdatedState(onBarcodeDetected)
    val currentCameraErrorCallback =
        androidx.compose.runtime.rememberUpdatedState(onCameraUnavailable)
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        BarcodeAnalyzer { barcode -> currentBarcodeCallback.value(barcode) }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
    )

    DisposableEffect(lifecycleOwner, previewView) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var disposed = false
        var boundProvider: ProcessCameraProvider? = null
        var boundPreview: Preview? = null
        var boundAnalysis: ImageAnalysis? = null

        providerFuture.addListener(
            {
                if (disposed) return@addListener
                runCatching {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(analyzerExecutor, analyzer) }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                    boundProvider = provider
                    boundPreview = preview
                    boundAnalysis = analysis
                }.onFailure {
                    currentCameraErrorCallback.value()
                }
            },
            mainExecutor,
        )

        onDispose {
            disposed = true
            val useCases = listOfNotNull(boundPreview, boundAnalysis).toTypedArray()
            if (useCases.isNotEmpty()) boundProvider?.unbind(*useCases)
            analyzer.close()
            analyzerExecutor.shutdown()
        }
    }
}

private class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer, AutoCloseable {
    private val isProcessing = AtomicBoolean(false)
    private val resultDelivered = AtomicBoolean(false)
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(groceryBarcodeScannerOptions())

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (resultDelivered.get() || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            isProcessing.set(false)
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val value = barcodes
                    .asSequence()
                    .mapNotNull(Barcode::getRawValue)
                    .firstOrNull { candidate ->
                        candidate.length in 8..14 && candidate.all(Char::isDigit)
                    }
                if (value != null && resultDelivered.compareAndSet(false, true)) {
                    onBarcodeDetected(value)
                }
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }

    override fun close() {
        scanner.close()
    }
}

internal fun groceryBarcodeScannerOptions(): BarcodeScannerOptions =
    BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
        )
        .build()
