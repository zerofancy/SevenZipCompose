package top.ntutn.sevenzip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException
import org.jetbrains.compose.resources.painterResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.icon
import top.ntutn.sevenzip.storage.GlobalSettingDataStore
import top.ntutn.sevenzip.ui.App
import top.ntutn.sevenzip.ui.SettingPage


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
    val settingDataStore = GlobalSettingDataStore()
    application {
        var settingOpened by remember { mutableStateOf(false) }
        val customDensity by settingDataStore
            .settingData()
            .map { Density(it.density, it.fontScale) }
            .collectAsState(Density(1f, 1f))
        Window(
            onCloseRequest = ::exitApplication,
            title = "SevenZip",
            icon = painterResource(Res.drawable.icon)
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
                        val scope = rememberCoroutineScope()
                        SettingPage(customDensity, onDensityChange = {
                            scope.launch {
                                settingDataStore.updateDensity(density = it.density, fontScale = it.fontScale)
                            }
                        })
                    }
                }
            }
        }
    }
}
