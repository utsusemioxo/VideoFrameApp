package com.example.videoframeapp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlin.jvm.java

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VideoFrameMainScreen(
                onRecordClick = {
                    startActivity(Intent(this, RecordActivity::class.java))
                },
                onProcessClick = {
                    startActivity(Intent(this, ProcessActivity::class.java))
                }
            )
        }

    }

}

@Composable
fun VideoFrameMainScreen(
    onRecordClick: () -> Unit,
    onProcessClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎬 Video Frame App",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Button(
                onClick = onRecordClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "📹 开始录像")
            }

            Button(
                onClick = onProcessClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "🧙‍♂️ 视频插帧处理")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVideoFrameMainScreen() {
    VideoFrameMainScreen(onRecordClick = {}, onProcessClick = {})
}