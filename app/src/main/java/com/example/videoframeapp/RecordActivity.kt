package com.example.videoframeapp

import android.content.ContentValues
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.lifecycle.compose.LocalLifecycleOwner

class RecordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                RecordScreen()
            }
        }
        Log.d("RecordActivity", "onCreate called")

    }

    override fun onPause() {
        super.onPause()
        Log.d("RecordActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("RecordActivity", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RecordActivity", "onDestroy called")
    }

}

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

fun playVideo(uri: Uri?) {

}

fun processVideo(uri: Uri?) {

}

fun showComparison(original: Uri?, processed: Uri?) {

}

@Composable
fun RecordScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var recording by remember { mutableStateOf<Recording?>(null)}
    var statusText by remember { mutableStateOf("准备录制 🎬") }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

    var recordedUri by remember { mutableStateOf<Uri?>(null) }
    var processedUri by remember { mutableStateOf<Uri?>(null) }

    val previewView = remember { PreviewView(context) }

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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = statusText, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val capture = videoCapture ?:return@Button
                if (recording == null) {
                    val name = "VID_${System.currentTimeMillis()}.mp4"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/VideoFrameApp")
                    }

                    val outputOptions = MediaStoreOutputOptions.Builder(
                        context.contentResolver,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    ).setContentValues(contentValues).build()

                    recording = capture.output
                        .prepareRecording(context, outputOptions)
                        .withAudioEnabled()
                        .start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> {
                                    statusText = "🔴 正在录像..."
                                }
                                is VideoRecordEvent.Finalize -> {
                                    if (event.hasError()) {
                                        Log.e("Record", "录像失败: ${event.error}")
                                    } else {
                                        statusText = "✅ 录像完成: ${event.outputResults.outputUri}"
                                        recording = null
                                        event.outputResults.outputUri?.let { uri ->
                                            Log.d("Record", "录像完成 URI = $uri")
                                            recordedUri = uri
                                            val path: String = uri.toString()
                                            VideoProcessor.processVideoSafe(path)
                                        } ?: Log.e("Record", "录像完成，但 URI 为空")
                                    }
                                }
                            }
                        }
                } else {
                    recording?.stop()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (recording == null) "🎥 开始录制" else "⏹ 停止录制" )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (recordedUri != null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { playVideo(recordedUri) }) { Text("▶ 原视频") }
                Button(onClick = { processVideo(recordedUri) }) { Text("⚡ 逐帧处理") }
                Button(onClick = { showComparison(recordedUri, processedUri) }) { Text("↔ 对比") }
            }
        }
    }
}


@Preview(showBackground=true, showSystemUi = true)
@Composable
fun RecordScreenPreview() {
    RecordScreen()
}