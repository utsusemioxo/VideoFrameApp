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
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNav()
        }

    }

}

@Composable
fun AppNav(
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "mainMenu"
    ) {

        // 注册 mainMenu
        composable("mainMenu") {
            // 将按钮点击事件绑定到导航
            VideoFrameMainScreen(navController = navController)
        }

        // 注册其他页面
        composable("record") {
            RecordScreen(navController = navController)
        }

        composable("process?videoUri={videoUri}",
            arguments = listOf(
                navArgument("videoUri") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val videoUriString = backStackEntry.arguments?.getString("videoUri")
            val videoUri = videoUriString?.takeIf { it.isNotEmpty() }?.toUri()

            ProcessScreen(
                initialVideoUri = videoUri,
                onProcessClick = {
                    videoUri?.let { uri ->
                        VideoProcessor.processVideoSafe(uri.toString())
                    }
                }
            )
        }


        // 对比页面
        composable(
            route = "compare?originalUri={originalUri}&processedUri={processedUri}",
            arguments = listOf(
                navArgument("originalUri") { defaultValue = "" },
                navArgument("processedUri") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val originalUri = backStackEntry.arguments?.getString("originalUri")?.toUri()
            val processedUri = backStackEntry.arguments?.getString("processedUri")?.toUri()
            CompareScreen(
                navController = navController,
                originalUri = originalUri,
                processedUri = processedUri
            )
        }
    }

}

@Composable
fun VideoFrameMainScreen(navController: androidx.navigation.NavHostController) {
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
                onClick = { navController.navigate("record") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "📹 开始录像")
            }

            Button(
                onClick = { navController.navigate("process") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "🧙‍♂️ 视频插帧处理")
            }
        }
    }
}



@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewAppNav() {
    AppNav()
}