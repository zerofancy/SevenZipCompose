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
import kotlinx.coroutines.runBlocking
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.app_name
import sevenzip.composeapp.generated.resources.icon
import sevenzip.composeapp.generated.resources.setting_window_title
import sevenzip.composeapp.generated.resources.title_template
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
    runBlocking {
        FileKit.init(getString(Res.string.app_name))
    }
    val settingDataStore = GlobalSettingDataStore()
    application {
        var settingOpened by remember { mutableStateOf(false) }
        val customDensity by settingDataStore
            .settingData()
            .map { Density(it.density, it.fontScale) }
            .collectAsState(Density(1f, 1f))
        var openedFileName: String? by remember { mutableStateOf(null) }
        val windowTitle = openedFileName?.let { fileName ->
            stringResource(Res.string.title_template, fileName)
        } ?: stringResource(Res.string.app_name)

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
                    title = stringResource(Res.string.setting_window_title)
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
