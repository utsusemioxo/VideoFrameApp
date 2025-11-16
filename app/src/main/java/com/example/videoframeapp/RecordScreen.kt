package com.example.videoframeapp

import android.content.ContentValues
import android.Manifest
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController


private fun startCamera(
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onReady: (VideoCapture<Recorder>) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture .addListener({
    val cameraProvider = cameraProviderFuture.get()

    val preview = androidx.camera.core.Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HD))
        .build()
    val videoCapture = VideoCapture.withOutput(recorder)

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        videoCapture
    )

        onReady(videoCapture)
    }, ContextCompat.getMainExecutor(context))
}

@Composable
fun RecordScreen(
    navController: androidx.navigation.NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var recording by remember { mutableStateOf<Recording?>(null) }
    var statusText by remember { mutableStateOf("准备录制 🎬") }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

    val previewView = remember { PreviewView(context) }

    // 权限
    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            startCamera(context, lifecycleOwner, previewView) { videoCapture = it }
        } else {
            Toast.makeText(context, "需要相机和录音权限！", Toast.LENGTH_SHORT).show()
        }
    }


    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
    }

    // 选择视频
    val selectVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { navController.navigate("process?videoUri=$it") }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing // safeDrawing = status + nav + ime
            )
    ) {
        // 相机预览
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing // safeDrawing = status + nav + ime
                )

        )

        // 底部悬浮按钮 + 状态文本
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 状态文本悬浮在按钮上方
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { selectVideoLauncher.launch("video/*") }) {
                    Text("📇 库")
                }

                Button(onClick = {
                    val capture = videoCapture ?: return@Button
                    if (recording == null) {
                        val name = "VID_${System.currentTimeMillis()}.mp4"
                        val outputOptions = MediaStoreOutputOptions.Builder(
                            context.contentResolver,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        ).setContentValues(ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/VideoFrameApp")
                        }).build()

                        recording = capture.output.prepareRecording(context, outputOptions)
                            .withAudioEnabled()
                            .start(ContextCompat.getMainExecutor(context)) { event ->
                                when (event) {
                                    is VideoRecordEvent.Start -> statusText = "🔴 正在录像..."
                                    is VideoRecordEvent.Finalize -> {
                                        if (!event.hasError()) {
                                            statusText = "✅ 录像完成"
                                            recording = null
                                            event.outputResults.outputUri?.let {
                                                navController.navigate("process?videoUri=$it")
                                            }
                                        } else {
                                            statusText = "❌ 录像失败: ${event.error}"
                                        }
                                    }
                                }
                            }
                    } else {
                        recording?.stop()
                    }
                }) {
                    Text(if (recording == null) "🎥 开始录制" else "⏹ 停止录制")
                }
            }
        }
    }
}



@Preview(showBackground=true, showSystemUi = false)
@Composable
fun RecordScreenPreview() {
    RecordScreen( navController = rememberNavController())
}