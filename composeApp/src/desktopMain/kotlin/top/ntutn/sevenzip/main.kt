package top.ntutn.sevenzip

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SevenZip",
    ) {
        val customDensity = Density(density = 1.75f, fontScale = 1.75f)

        CompositionLocalProvider(
            LocalDensity provides customDensity
        ) {
            App()
        }
    }
}