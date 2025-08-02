package top.ntutn.sevenzip.toast

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalToastController = staticCompositionLocalOf<IToastController> {
    error("No ToastController Provided. Make sure to wrap your app with ToastHost.")
}

@Composable
fun ToastHost(content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember(coroutineScope) {
        DefaultToastController(coroutineScope)
    }

    CompositionLocalProvider(LocalToastController provides controller) {
        content()

        val messages by remember { derivedStateOf { controller.getMessages() }}

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            messages.forEachIndexed { index, message ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color.Black.copy(alpha = 0.7f),
                    tonalElevation = 16.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .padding(bottom = 24.dp + 48.dp * index)
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