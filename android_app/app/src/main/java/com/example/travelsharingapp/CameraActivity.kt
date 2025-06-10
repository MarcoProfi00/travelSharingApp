package com.example.travelsharingapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelsharingapp.data.repository.ThemeRepository
import com.example.travelsharingapp.ui.screens.main.LocalWindowSizeClass
import com.example.travelsharingapp.ui.screens.settings.ThemeViewModel
import com.example.travelsharingapp.ui.screens.settings.ThemeViewModelFactory
import com.example.travelsharingapp.ui.theme.TravelProposalTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

class CameraActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(ThemeRepository(context))
            )
            val currentThemeSetting by themeViewModel.themeSetting.collectAsState()

            val windowSizeClass = calculateWindowSizeClass(this)

            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                TravelProposalTheme(
                    themeSetting = currentThemeSetting,
                    dynamicColor = false
                ) {
                    CameraScreen(
                        onImageCaptured = { uri ->
                            setResult(RESULT_OK, Intent().setData(uri))
                            finish()
                        },
                        onError = { exception ->
                            Toast.makeText(
                                applicationContext,
                                "Camera Error: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(FLASH_MODE_OFF) }
    var showGrid by remember { mutableStateOf(false) }

    var rotation by remember { mutableIntStateOf(Surface.ROTATION_0) }
    val orientationEventListener = rememberOrientationEventListener(context) { orientation ->
        rotation = when (orientation) {
            in 45..134 -> Surface.ROTATION_270
            in 135..224 -> Surface.ROTATION_180
            in 225..314 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                orientationEventListener.enable()
            }
            if (event == Lifecycle.Event.ON_STOP) {
                orientationEventListener.disable()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            orientationEventListener.disable()
        }
    }

    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder()
            .build()
    }

    val preview: Preview = remember {
        Preview.Builder().build()
    }
    val previewView = remember { PreviewView(context) }
    var camera: Camera? by remember { mutableStateOf(null) }

    LaunchedEffect(rotation, preview, imageCapture) {
        preview.targetRotation = rotation
        imageCapture.targetRotation = rotation
    }

    LaunchedEffect(lensFacing) {
        preview.surfaceProvider = previewView.surfaceProvider
        camera = setupCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            preview = preview,
            imageCapture = imageCapture,
            lensFacing = lensFacing,
            onError = onError
        )
        imageCapture.flashMode = flashMode
    }

    val targetRotationDegrees = when (rotation) {
        Surface.ROTATION_0 -> 0f
        Surface.ROTATION_90 -> 90f
        Surface.ROTATION_180 -> 180f
        Surface.ROTATION_270 -> 270f
        else -> 0f
    }

    val animatedRotationDegrees by animateFloatAsState(
        targetValue = targetRotationDegrees,
        animationSpec = tween(durationMillis = 300)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { "" },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    scrolledContainerColor = Color.Black
                ),
                modifier = Modifier.height(80.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.Black)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    flashMode = when (flashMode) {
                        FLASH_MODE_OFF -> FLASH_MODE_ON
                        FLASH_MODE_ON -> FLASH_MODE_AUTO
                        else -> FLASH_MODE_OFF
                    }
                    imageCapture.flashMode = flashMode
                }) {
                    Icon(
                        imageVector = when (flashMode) {
                            FLASH_MODE_ON -> Icons.Default.FlashOn
                            FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash Mode",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(animatedRotationDegrees)
                    )
                }

                IconButton(onClick = { showGrid = !showGrid }) {
                    Icon(
                        imageVector = if (showGrid) Icons.Default.GridOff else Icons.Default.GridOn,
                        contentDescription = "Grid Lines",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(animatedRotationDegrees)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxSize()
                )

                if (showGrid) {
                    GridLines()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    // nothing
                }

                IconButton(
                    modifier = Modifier
                        .size(80.dp)
                        .border(3.dp, Color.White, CircleShape)
                        .rotate(animatedRotationDegrees),
                    onClick = {
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onImageSaved = onImageCaptured,
                            onError = onError
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Take Photo",
                        tint = Color.White,
                        modifier = Modifier
                            .size(60.dp)
                            .padding(10.dp)
                    )
                }

                IconButton(onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
                }) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .rotate(animatedRotationDegrees)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberOrientationEventListener(
    context: Context,
    onOrientationChanged: (Int) -> Unit
): OrientationEventListener {
    val eventListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                onOrientationChanged(orientation)
            }
        }
    }
    return eventListener
}

@Composable
fun GridLines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        val color = Color.White.copy(alpha = 0.5f)
        val width = size.width
        val height = size.height

        // Vertical lines
        drawLine(color, Offset(width / 3, 0f), Offset(width / 3, height), strokeWidth)
        drawLine(color, Offset(2 * width / 3, 0f), Offset(2 * width / 3, height), strokeWidth)

        // Horizontal lines
        drawLine(color, Offset(0f, height / 3), Offset(width, height / 3), strokeWidth)
        drawLine(color, Offset(0f, 2 * height / 3), Offset(width, 2 * height / 3), strokeWidth)
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    preview: Preview,
    imageCapture: ImageCapture,
    lensFacing: Int,
    onError: (Exception) -> Unit
): Camera? {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val mainExecutor = ContextCompat.getMainExecutor(context)
    var camera: Camera? = null

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            preview.surfaceProvider = previewView.surfaceProvider

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            preview.targetRotation = previewView.display.rotation
            imageCapture.targetRotation = previewView.display.rotation
        } catch (exc: Exception) {
            onError(exc)
            camera = null
        }
    }, mainExecutor)

    return camera
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageSaved: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/TravelSharingApp")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        mainExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let {
                    onImageSaved(it)
                } ?: onError(IllegalStateException("Image URI was null after saving"))
            }

            override fun onError(exc: ImageCaptureException) {
                onError(exc)
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraScreenPreview() {
    CameraScreen(
        onImageCaptured = { },
        onError = { }
    )
}