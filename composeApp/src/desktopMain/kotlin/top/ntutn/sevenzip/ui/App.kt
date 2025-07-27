package top.ntutn.sevenzip.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.skiko.hostOs
import top.ntutn.sevenzip.SevenZipViewModel
import top.ntutn.sevenzip.util.FileIconFetcher
import top.ntutn.sevenzip.util.FileIconUtils
import top.ntutn.sevenzip.util.LinuxFileIconProvider
import java.io.File


@Composable
@Preview
fun App(onOpenSetting: () -> Unit = {}) {
    MaterialTheme {
        Column {
            val viewModel = viewModel(SevenZipViewModel::class)

            // toolbar
            Row {
                val scope = rememberCoroutineScope()
                Button(onClick = {
                    scope.launch {
                        val kitFile = FileKit.openFilePicker()?.file ?: return@launch
                        viewModel.openArchive(kitFile)
                    }
                }) {
                    Text("Open")
                }
                Button(onClick = onOpenSetting) {
                    Text("Setting")
                }
            }
            // content
            Box {
                val currentNode by viewModel.browsingNode.collectAsState()
                Column {
                    val node = currentNode
                    if (node == null) {
                        Text("No open file now")
                    } else {
                        Button(onClick = {
                            viewModel.moveBack()
                        }) {
                            Text("Back")
                        }
                        LazyColumn {
                            items(node.children) { childrenNode ->
                                Row {
                                    var iconPainter by remember { mutableStateOf<Painter>(
                                        ColorPainter(Color.LightGray)) }
                                    if (hostOs.isWindows) {
                                        LaunchedEffect(childrenNode) {
                                            // 在后台线程获取图标
                                            withContext(Dispatchers.IO) {
                                                val extension = childrenNode.name.split(".").last()
                                                val dummyFile = if (childrenNode.isDir) {
                                                    FileKit.cacheDir.file
                                                } else if (extension.isBlank()) {
                                                    File(FileKit.cacheDir.file, "dummy")
                                                } else {
                                                    File(FileKit.cacheDir.file, "dummy.${extension}")
                                                }
                                                if (!dummyFile.exists()) {
                                                    dummyFile.createNewFile()
                                                }
                                                val fileIcon = FileIconFetcher.getFileIcon(
                                                    path = dummyFile.canonicalPath,
                                                    isDirectory = childrenNode.isDir,
                                                    isLarge = true
                                                )
                                                fileIcon?.let {
                                                    iconPainter = it.toPainter()
                                                }
                                            }
                                        }
                                    } else if(hostOs.isLinux) {
                                        LaunchedEffect(childrenNode) {
                                            withContext(Dispatchers.IO) {
                                                val extension = childrenNode.name.split(".").last()
                                                val dummyFile = if (childrenNode.isDir) {
                                                    FileKit.cacheDir.file
                                                } else if (extension.isBlank()) {
                                                    File(FileKit.cacheDir.file, "dummy")
                                                } else {
                                                    File(FileKit.cacheDir.file, "dummy.${extension}")
                                                }
                                                val icon = LinuxFileIconProvider.getFileIcon(dummyFile.canonicalPath)
                                                icon?.let {
                                                    iconPainter = BitmapPainter(it)
                                                }
                                            }
                                        }
                                    } else {
                                        val resource by derivedStateOf {
                                            FileIconUtils.getIconPath(childrenNode.isDir, childrenNode.name)
                                        }
                                        iconPainter = painterResource(resource)
                                    }
                                    Image(
                                        painter = iconPainter,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(childrenNode.name)
                                    if (childrenNode.isDir) {
                                        Button(onClick = {
                                            viewModel.enterFolder(childrenNode)
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
        }
    }
}