package com.example.videoframeapp

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * CompareScreen - 左右对比原视频和处理后视频
 *
 * @param navController 用于返回上一页面
 * @param originalUri 原视频 Uri
 * @param processedUri 处理后视频 Uri
 */
@Composable
fun CompareScreen(
    navController: NavController,
    originalUri: Uri? = null,
    processedUri: Uri? = null
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "视频对比 🎞️",
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 原视频
                VideoPlayerView(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    videoUri = originalUri,
                    label = "原视频"
                )

                // 处理后视频
                VideoPlayerView(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    videoUri = processedUri,
                    label = "处理后"
                )
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("⬅ 返回")
            }
        }
    }
}

/**
 * VideoPlayerView - 播放单个视频
 *
 * 使用 AndroidView 包裹 VideoView
 */
@Composable
fun VideoPlayerView(
    modifier: Modifier = Modifier,
    videoUri: Uri?,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)

        if (videoUri != null) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black, RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("未选择视频", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCompareScreen() {
    CompareScreen(
        navController = rememberNavController(),
        originalUri = null,   // 这里可以传测试 Uri
        processedUri = null
    )
}
