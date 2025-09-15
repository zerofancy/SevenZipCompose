package top.ntutn.sevenzip.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState

val LocalToastController = staticCompositionLocalOf<IToastController> {
    error("No ToastController Provided. Make sure to wrap your app with ToastHost.")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ToastHost(content: @Composable () -> Unit) {
    val toaster = rememberToasterState()

    val controller = remember(toaster) { SonnerToastController(toaster) }

    CompositionLocalProvider(LocalToastController provides controller) {
        content()

        Toaster(state = toaster)
    }
}
