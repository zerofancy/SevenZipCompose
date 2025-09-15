package top.ntutn.sevenzip.toast

import com.dokar.sonner.ToasterState
import kotlin.time.Duration.Companion.milliseconds

class SonnerToastController(private val toaster: ToasterState): IToastController {
    override fun show(text: String, duration: Long) {
        toaster.show(text, duration = duration.milliseconds)
    }
}