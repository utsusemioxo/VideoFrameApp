package com.example.videoframeapp

import android.content.ContentValues
import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat

class RecordActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnRecord: Button
    private lateinit var tvStatus: TextView

    private var recording: Recording? = null
    private lateinit var videoCapture: VideoCapture<Recorder>

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.all { it.value }
            if (granted) {
                startCamera() // ✅ 权限拿到后再启动相机
            } else {
                Toast.makeText(this, "需要相机和录音权限才能录制视频", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        previewView = findViewById(R.id.previewView)
        btnRecord = findViewById(R.id.btnRecord)
        tvStatus = findViewById(R.id.tvStatus)

        checkPermissionsAndStartCamera()

        btnRecord.setOnClickListener {
            if (recording != null) {
                stopRecording()
            } else {
                startRecording()
            }
        }

    }

    private fun checkPermissionsAndStartCamera() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // preview config
            val preview = Preview.Builder().build()
            preview.surfaceProvider = previewView.surfaceProvider

            // record config
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // bind life cycle
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        val name = "VID_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideoFrameApp")
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        // call CameraX recording API
        recording = videoCapture.output
            .prepareRecording(this, outputOptions)
            .apply {
                // 如果想要接管底层数据，可以这里使用`.withAudioEnabled()` 或自定义 Output
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        btnRecord.text = "⏹ 停止录像"
                        tvStatus.text = "正在录像..."
                    }
                    is VideoRecordEvent.Finalize -> {
                        btnRecord.text = "\uD83C\uDFAC 开始录像"
                        tvStatus.text = "录像完成: ${recordEvent.outputResults.outputUri}"
                        recording = null
                        // 可以在这里调用 JNI pipeline 处理视频
                        recordEvent.outputResults.outputUri?.let { uri->
                            VideoProcessor.processVideo(uri.toString())
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        recording?.stop()
    }
}