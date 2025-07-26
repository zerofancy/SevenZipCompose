package top.ntutn.sevenzip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException


@OptIn(ExperimentalMaterial3Api::class)
fun main() {
    try {
        SevenZip.initSevenZipFromPlatformJAR()
        println("7-Zip-JBinding library was initialized")
    } catch (e: SevenZipNativeInitializationException) {
        e.printStackTrace()
        error(e)
    }
    FileKit.init("SevenZip")
    application {
        var settingOpened by remember { mutableStateOf(false) }
        var customDensity by remember { mutableStateOf(Density(density = 1f, fontScale = 1f)) }
        Window(
            onCloseRequest = ::exitApplication,
            title = "SevenZip",
        ) {
            CompositionLocalProvider(
                LocalDensity provides customDensity
            ) {
                App(onOpenSetting = {
                    settingOpened = true
                })
            }
            if (settingOpened) {
                DialogWindow(
                    onCloseRequest = { settingOpened = false },
                    title = "设置"
                ) {
                    CompositionLocalProvider(
                        LocalDensity provides customDensity
                    ) {
                        Column {
                            Row {
                                Text("Scale")
                                val sliderState = remember {
                                    SliderState(
                                        value = 1f,
                                        steps = 0,
                                        onValueChangeFinished = {},
                                        valueRange = 0.5f..5f
                                    )
                                }
                                // 这里由Slider来做交互体验有点奇怪，但目前能用
                                sliderState.onValueChangeFinished = {
                                    customDensity = Density(sliderState.value, sliderState.value)
                                }
                                Slider(sliderState)
                            }
                        }
                    }
                }
            }
        }
    }
}