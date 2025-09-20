package top.ntutn.sevenzip.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState

val LocalToaster = staticCompositionLocalOf<ToasterState> { error("No ToasterState Provided. Make sure to wrap your app with ToastHost.") }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ToastHost(content: @Composable () -> Unit) {
    val toaster = rememberToasterState()

    CompositionLocalProvider(LocalToaster provides toaster) {
        content()

        Toaster(state = toaster)
    }
}
