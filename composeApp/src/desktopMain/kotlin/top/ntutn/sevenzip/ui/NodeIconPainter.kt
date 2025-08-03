package top.ntutn.sevenzip.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.hostOs
import top.ntutn.sevenzip.zip.ArchiveNode
import top.ntutn.sevenzip.util.FileIconFetcher
import top.ntutn.sevenzip.util.FileIconUtils
import top.ntutn.sevenzip.util.LinuxFileIconProvider
import java.io.File

private val iconCache = mutableMapOf<File, Painter?>()

@Composable
fun NodeIconPainter(node: ArchiveNode, tryUseSystemIcon: Boolean, onPainterLoaded: (Painter?) -> Unit) {
    val callback by rememberUpdatedState(onPainterLoaded)
    if (tryUseSystemIcon && hostOs.isWindows && FileIconFetcher.tryInit()) {
        LaunchedEffect(node) {
            withContext(Dispatchers.IO) {
                val dummyFile = obtainDummyFile(node)
                val painter = loadIconWithCache(dummyFile) {
                    val fileIcon = FileIconFetcher.getFileIcon(
                        path = dummyFile.canonicalPath,
                        isDirectory = node.isDir,
                        isLarge = true
                    )
                    fileIcon?.toPainter()
                }
                withContext(Dispatchers.Main) {
                    callback(painter)
                }
            }
        }
    } else if (tryUseSystemIcon && hostOs.isLinux && LinuxFileIconProvider.tryInit()) {
        LaunchedEffect(node) {
            val dummyFile = obtainDummyFile(node)
            val painter = loadIconWithCache(dummyFile) {
                withContext(Dispatchers.Main) {
                    val icon = LinuxFileIconProvider.getFileIcon(dummyFile.canonicalPath)
                    icon?.let { image -> BitmapPainter(image) }
                }
            }
            withContext(Dispatchers.Main) {
                callback(painter)
            }
        }
    } else {
        val resource by derivedStateOf {
            FileIconUtils.getIconPath(node.isDir, node.name)
        }
        callback(painterResource(resource))
    }
}

private suspend fun obtainDummyFile(node: ArchiveNode): File {
    val extension = node.name.split(".").last()
    val dummyFile = if (node.isDir) {
        FileKit.cacheDir.file
    } else if (extension.isBlank()) {
        File(FileKit.cacheDir.file, "dummy")
    } else {
        File(FileKit.cacheDir.file, "dummy.${extension}")
    }
    if (!dummyFile.exists()) {
        withContext(Dispatchers.IO) {
            dummyFile.createNewFile()
        }
    }
    return dummyFile
}

private suspend fun loadIconWithCache(
    dummyFile: File,
    realLoader: suspend (File) -> Painter?
): Painter? = withContext(Dispatchers.Main) {
    if (iconCache.contains(dummyFile)) {
        return@withContext iconCache[dummyFile]
    }
    val painter = realLoader(dummyFile)
    iconCache[dummyFile] = painter
    return@withContext painter
}
