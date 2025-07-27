package top.ntutn.sevenzip.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.hostOs
import top.ntutn.sevenzip.ArchiveNode
import top.ntutn.sevenzip.util.FileIconFetcher
import top.ntutn.sevenzip.util.FileIconUtils
import top.ntutn.sevenzip.util.LinuxFileIconProvider
import java.io.File

@Composable
fun ContentArea(
    currentNode: ArchiveNode?,
    modifier: Modifier = Modifier.Companion,
    onEnterDir: (ArchiveNode) -> Unit = {}
) {
    Column(modifier = modifier) {
        if (currentNode == null) {
            Text("No open file now")
        } else {
            LazyColumn {
                items(currentNode.children) { childrenNode ->
                    Row {
                        var iconPainter by remember {
                            mutableStateOf<Painter>(
                                ColorPainter(Color.Companion.LightGray)
                            )
                        }
                        NodeIconPainter(childrenNode) {
                            if (it != null) {
                                iconPainter = it
                            }
                        }

                        Image(
                            painter = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.Companion.size(48.dp)
                        )
                        Text(childrenNode.name)
                        if (childrenNode.isDir) {
                            Button(onClick = {
                                onEnterDir(childrenNode)
                            }) {
                                Text("Browse")
                            }
                        }
                    }
                }
            }
        }
    }
}

private val iconCache = mutableMapOf<File, Painter?>()

@Composable
private fun NodeIconPainter(node: ArchiveNode, onPainterLoaded: (Painter?) -> Unit) {
    val callback by rememberUpdatedState(onPainterLoaded)
    if (hostOs.isWindows) {
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
    } else if (hostOs.isLinux) {
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
