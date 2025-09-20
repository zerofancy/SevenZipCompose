package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.new_window_add_file_button
import sevenzip.composeapp.generated.resources.new_window_add_folder_button
import sevenzip.composeapp.generated.resources.new_window_create_archive
import sevenzip.composeapp.generated.resources.new_window_create_folder_button
import sevenzip.composeapp.generated.resources.new_window_empty_tip
import sevenzip.composeapp.generated.resources.new_window_invalid_filename_tip
import sevenzip.composeapp.generated.resources.new_window_upward_button
import top.ntutn.sevenzip.toast.LocalToaster
import top.ntutn.sevenzip.util.UniversalFileNameValidator
import top.ntutn.sevenzip.util.toReferenceCounted
import top.ntutn.sevenzip.zip.ArchiveNode

@Composable
fun AddPage(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        val rootArchiveNode = remember {
            ArchiveNode().also { // 这是个根节点，不会打到压缩包里
                it.name = "ROOT_NODE"
                it.isDir = true
            }
        }
        var currentNodeRef by remember { mutableStateOf(rootArchiveNode.toReferenceCounted { }) }
        val currentNode = currentNodeRef.get()
        Box(
            modifier = Modifier.fillMaxHeight().weight(1f),
            contentAlignment = Alignment.Center
        ) { // 左边布局，文件区域
            if (currentNode == rootArchiveNode && currentNode.children.isEmpty()) {
                Text(stringResource(Res.string.new_window_empty_tip))
            } else {
                ContentArea(
                    currentNode = currentNodeRef,
                    tryUseSystemIcon = true,
                    onAccessNode = {
                        if (it.isDir) {
                            currentNodeRef = it.toReferenceCounted { }
                        }
                    }
                )
            }
        }
        Box(modifier = Modifier.width(256.dp).fillMaxHeight()) { // 右边布局，按钮和操作区域
            Surface(
                modifier = Modifier.fillMaxSize(),
                shadowElevation = 4.dp,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    val scope = rememberCoroutineScope()
                    val toaster = LocalToaster.current

                    OutlinedButton(
                        onClick = {
                            currentNodeRef =
                                (currentNode.parent ?: rootArchiveNode).toReferenceCounted { }
                        },
                        enabled = currentNode != rootArchiveNode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.new_window_upward_button))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = {
                        scope.launch {
                            val file = FileKit.openFilePicker()?.file ?: return@launch
                            val node = currentNode
                            val hasSameFile =
                                node.children.find { node -> node.name == file.name } != null
                            if (hasSameFile) {
                                toaster.show(getString(Res.string.new_window_invalid_filename_tip))
                                return@launch
                            }
                            node.children.add(ArchiveNode().also {
                                it.name = file.name
                                it.isDir = false
                                it.parent = node
                                it.originFile = file
                            })
                            currentNodeRef = node.toReferenceCounted { }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.new_window_add_file_button))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = {
                        scope.launch {
                            val rootFile = FileKit.openDirectoryPicker()?.file ?: return@launch
                            val rootNode = ArchiveNode().also {
                                it.name = rootFile.name
                                it.isDir = true
                            }
                            var buildingDirNode = rootNode
                            rootFile.walkTopDown().onEnter { dir ->
                                if (dir == rootFile) {
                                    // rootNode已经创建，不需要再次创建
                                    return@onEnter true
                                }
                                val node = ArchiveNode().also {
                                    it.name = dir.name
                                    it.isDir = true
                                    it.parent = buildingDirNode
                                }
                                buildingDirNode.children.add(node)
                                buildingDirNode = node
                                true
                            }.onLeave { dir ->
                                buildingDirNode = buildingDirNode.parent ?: rootNode
                            }.onEach { file ->
                                if (file.isDirectory) {
                                    return@onEach
                                }
                                buildingDirNode.children.add(ArchiveNode().also {
                                    it.name = file.name
                                    it.isDir = false
                                    it.parent = buildingDirNode
                                    it.originFile = file
                                })
                            }.toList()

                            val node = currentNode
                            node.children.add(rootNode)
                            currentNodeRef = node.toReferenceCounted { }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.new_window_add_folder_button))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    var inputtingDirName by remember { mutableStateOf(false) }
                    if (inputtingDirName) {
                        var inputtingName by remember { mutableStateOf("") }
                        AlertDialog(onDismissRequest = {
                            inputtingDirName = false
                        }, confirmButton = {
                            Button(onClick = {
                                // 1. 检查文件名是否重复 todo 是否大小写敏感
                                val hasSame =
                                    currentNode.children.any { node -> node.name == inputtingName }
                                if (hasSame) {
                                    toaster.show("文件名重复")
                                    return@Button
                                }

                                // 2. 检查文件名是否合法
                                if (!UniversalFileNameValidator.isFileNameValidForAllPlatforms(
                                        inputtingName
                                )) {
                                    toaster.show("文件名不合法")
                                    return@Button
                                }

                                // 3. 创建目录
                                val newNode = ArchiveNode().also {
                                    it.name = inputtingName
                                    it.isDir = true
                                    it.parent = currentNode
                                }
                                currentNode.children.add(newNode)
                                currentNodeRef = currentNode.toReferenceCounted {  }

                                // 4. 关闭窗口
                                inputtingDirName = false
                            }) {
                                Text("确定")
                            }
                        }, dismissButton = {
                            OutlinedButton(onClick = {
                                inputtingDirName = false
                            }) {
                                Text("取消")
                            }
                        }, title = {
                            Text("创建目录")
                        }, text = {
                            TextField(value = inputtingName, onValueChange = {
                                inputtingName = it
                            })
                        })
                    }
                    OutlinedButton(onClick = {
                        inputtingDirName = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.new_window_create_folder_button))
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    Button(
                        onClick = { TODO() },
                        enabled = currentNode != rootArchiveNode || currentNode.children.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.new_window_create_archive))
                    }
                }
            }
        }
    }
}