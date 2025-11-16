package com.example.videoframeapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun ProcessScreen(
    initialVideoUri: Uri?,
    onProcessClick: (Uri) -> Unit
) {
    var videoUriState by remember { mutableStateOf(initialVideoUri) }
    var selectedFactor by remember { mutableStateOf(4) }
    var processing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val selectVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            videoUriState = it               // ⚡ 更新 UI
            onProcessClick(it)               // ⚡ 传递选中视频
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "视频处理 🎞️",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "选择的视频: ${videoUriState?.lastPathSegment ?: "未选择"}",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PressableOption(
                    text = "x4",
                    selected = selectedFactor == 4,
                    onClick = { selectedFactor = 4}
                )
                PressableOption(
                    text = "x8",
                    selected = selectedFactor == 8,
                    onClick = { selectedFactor = 8}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (processing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("处理进度: ${(progress * 100).toInt()}%", fontSize = 14.sp)
                }
            } else {
                Button(
                    onClick = { videoUriState?.let { onProcessClick(it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⚡ 开始处理${selectedFactor}倍插帧")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { selectVideoLauncher.launch("video/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📇 库")
                }
            }
        }
    }
}


@Composable
fun PressableOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.primaryContainer
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (selected || isPressed) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        modifier = Modifier.clickable(interactionSource = interactionSource, indication = null) {
            onClick()
        }
    ) {
        Text(
            text = text,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProcessScreen() {
    ProcessScreen(initialVideoUri = null, onProcessClick = {})
}