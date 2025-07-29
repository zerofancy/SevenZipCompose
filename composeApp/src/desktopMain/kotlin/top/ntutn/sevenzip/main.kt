package top.ntutn.sevenzip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
        var openedFileName: String? by remember { mutableStateOf(null) }
        val windowTitle by derivedStateOf {
            if (openedFileName != null) {
                "SevenZip - $openedFileName"
            } else {
                "SevenZip"
            }
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = windowTitle,
            icon = painterResource(Res.drawable.icon)
        ) {
            val tryUseSystemIcon by settingDataStore.settingData()
                .map { it.tryUseSystemIcon }
                .collectAsState(false)
            CompositionLocalProvider(
                LocalDensity provides customDensity
            ) {
                App(
                    tryUseSystemIcon = tryUseSystemIcon,
                    onOpenSetting = {
                        settingOpened = true
                    },
                    onOpenFileNameChange = {
                        openedFileName = it
                    }
                )
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
                        SettingPage(
                            customDensity,
                            useSystemIcon = tryUseSystemIcon,
                            onDensityChange = {
                                scope.launch {
                                    settingDataStore.updateDensity(
                                        density = it.density,
                                        fontScale = it.fontScale
                                    )
                                }
                            }, onUseSystemIconChange = {
                                scope.launch {
                                    settingDataStore.updateUseSystemIcon(it)
                                }
                            })
                    }
                }
            }
        }
    }
}
