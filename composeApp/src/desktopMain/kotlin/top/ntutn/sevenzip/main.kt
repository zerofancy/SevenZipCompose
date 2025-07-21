package top.ntutn.sevenzip

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.RandomAccessFile


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
        LaunchedEffect(Unit) {
            try {
                SevenZip.initSevenZipFromPlatformJAR()
                println("7-Zip-JBinding library was initialized")
            } catch (e: SevenZipNativeInitializationException) {
                e.printStackTrace()
            }
            val kitFile = FileKit.openFilePicker()?.file ?: return@LaunchedEffect
            val file = RandomAccessFile(kitFile, "r")
            val archive = SevenZip.openInArchive(null, // auto select
                RandomAccessFileInStream(file))
            println("File count: ${archive.numberOfItems}")
            archive.close()
            file.close()
        }
    }
}