package com.example.videoframeapp

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun CompareScreenSlide(
    navController: NavController,
    originalUri: Uri? = null,
    processedUri: Uri? = null
) {
    val context = LocalContext.current

    // 滑块初始位置：50%
    var sliderPosition by remember { mutableStateOf(0.5f) }

    // 获取屏幕宽度
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val density = LocalDensity.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 标题
            Text(
                text = "视频对比 🎞️",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // 视频区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Black)
            ) {
                // 原视频：底层
                if (originalUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(originalUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 处理后视频：上层，宽度随滑块变化
                if (processedUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(processedUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(sliderPosition) // 宽度比例
                            .align(Alignment.CenterStart)
                    )
                }

                // 中间滑块
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .offset {
                            // 将 dp 转 px
                            IntOffset(
                                x = (sliderPosition * with(density) { screenWidth.toPx() }).toInt(),
                                y = 0
                            )
                        }
                        .background(Color.White)
                        .align(Alignment.CenterStart)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                val widthPx = with(density) { screenWidth.toPx() }
                                sliderPosition = (sliderPosition + dragAmount / widthPx).coerceIn(0f, 1f)
                            }
                        }
                )
            }

            // 返回按钮
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
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCompareScreenSlide() {
    CompareScreenSlide(
        navController = rememberNavController(),
        originalUri = null,
        processedUri = null
    )
}

