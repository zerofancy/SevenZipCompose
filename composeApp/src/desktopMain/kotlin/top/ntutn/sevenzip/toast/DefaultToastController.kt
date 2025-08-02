package top.ntutn.sevenzip.toast

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DefaultToastController(private val coroutineScope: CoroutineScope): IToastController {
    private val messages = mutableStateListOf<ToastMessage>()

    override fun show(text: String, duration: Long) {
        coroutineScope.launch {
            val toastMessage = ToastMessage(text, duration)
            messages.add(toastMessage)

            delay(duration)
            messages.remove(toastMessage)
        }
    }

    override fun getMessages(): List<ToastMessage> = messages.toList()
}