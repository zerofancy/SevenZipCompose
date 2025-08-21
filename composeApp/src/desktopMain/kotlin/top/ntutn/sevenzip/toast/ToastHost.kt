package top.ntutn.sevenzip.toast

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay

val LocalToastController = staticCompositionLocalOf<IToastController> {
    error("No ToastController Provided. Make sure to wrap your app with ToastHost.")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.ToastHost(content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember(coroutineScope) {
        DefaultToastController(coroutineScope)
    }

    // 获取主窗口引用（当前活跃窗口）
    val mainWindow = window

    CompositionLocalProvider(LocalToastController provides controller) {
        content()

        val messages by remember { derivedStateOf { controller.getMessages() } }

        messages.forEachIndexed { index, message ->
            val windowState = rememberWindowState(
                size = DpSize(Dp.Unspecified, Dp.Unspecified)
            )
            val density = LocalDensity.current
            Window(
                onCloseRequest = {},
                state = windowState,
                decoration = WindowDecoration.Undecorated(),
                alwaysOnTop = true,
                focusable = false,
                transparent = true,
                resizable = false,
                enabled = true,
            ) {
                // 设置Toast窗口位置在主窗口底部
                LaunchedEffect(mainWindow) {
                    mainWindow.let { parentWindow ->
                        // 等待窗口初始化完成
                        delay(10)

                        // 计算位置：主窗口底部中心，距离底部24dp
                        // 计算目标位置（转换为Dp单位）
                        val targetX = parentWindow.x.dp + (parentWindow.width.dp / 2) - (windowState.size.width / 2)
                        val targetY = parentWindow.y.dp + parentWindow.height.dp - 80.dp - (index * 60).dp
                        windowState.position = WindowPosition(targetX, targetY)
                    }
                }

                CompositionLocalProvider(
                    LocalDensity provides density
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = Color.Black.copy(alpha = 0.7f),
                        tonalElevation = 16.dp,
                        shadowElevation = 16.dp,
                        modifier = Modifier
                    ) {
                        Text(
                            text = message.text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
