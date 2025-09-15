package top.ntutn.sevenzip.toast

interface IToastController {
    fun show(text: String, duration: Long = 3000)
}

